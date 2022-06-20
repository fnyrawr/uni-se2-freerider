package de.freerider.restapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.freerider.datamodel.Customer;

import de.freerider.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
class CustomersController implements CustomersAPI {

    @Autowired
    private ApplicationContext context;
    //
    private final ObjectMapper objectMapper;
    //
    private final HttpServletRequest request;
    //
    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Constructor.
     *
     * @param objectMapper entry point to JSON tree for the Jackson library
     * @param request HTTP request object
     */
    public CustomersController( ObjectMapper objectMapper, HttpServletRequest request ) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    /**
     * GET /customers
     * @return JSON Array with customers (compact).
     */
    @Override
    public ResponseEntity<List<?>> getCustomers() {
        ResponseEntity<List<?>> re = null;
        System.err.println( request.getMethod() + " " + request.getRequestURI() );
        try {
            ArrayNode arrayNode = customersAsJSON();
            ObjectReader reader = objectMapper.readerFor(new TypeReference<List<ObjectNode>>() { } );
            List<String> list = reader.readValue( arrayNode );
            //
            re = new ResponseEntity<>( list, HttpStatus.OK );

        } catch( IOException e ) {
            re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return re;
    }

    /**
     * GET /customers/{id}
     * @return JSON Array with customers (compact).
     */
    @Override
    public ResponseEntity<?> getCustomer( @PathVariable("id") long id ) {
        ResponseEntity<?> re = null;
        System.err.println( request.getMethod() + " " + request.getRequestURI() );
        try {
            ArrayNode arrayNode = customerAsJSON(id);
            ObjectReader reader = objectMapper.readerFor(new TypeReference<List<ObjectNode>>() { } );
            String customer = reader.readValue( arrayNode ).toString();
            //
            if(customer == "[]") {
                re = new ResponseEntity<String>(HttpStatus.NOT_FOUND);
            }
            else {
                re = new ResponseEntity<>( customer, HttpStatus.OK );
            }

        } catch( IOException e ) {
            re = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return re;
    }

    /**
     * POST /customers
     *
     * Add new customers from JSON data passed as array of JSON objects in the request body.
     * Multiple customers can be posted with multiple JSON objects from the same request.
     * Id's are assigned, if id-attributes are missing or are empty in JSON data.
     *
     * JSON data containing id of objects that are already present are rejected. Rejected
     * objects are returned in the response with error 409 (conflict).
     *
     * Status 201 (created) is returned with empty array of conflicts when all objects were
     * accepted. Partial acceptance of objects from the request is possible, but error 409 is
     * returned with the array of rejected objects.
     *
     * @param jsonMap array of maps with raw JSON {@code <key,obj>}-data.
     * @return JSON array with the rejected JSON objects, empty array [] if all objects were accepted.
     */

    /*
     * Swagger API doc annotations:
     */
    @Override
    public ResponseEntity<List<?>> postCustomers(Map<String, Object>[] jsonMap ) {
        System.err.println( "POST /customers" );
        if( jsonMap == null )
            return new ResponseEntity<>( null, HttpStatus.BAD_REQUEST );
        //
        List<Customer> acceptedCustomers = new ArrayList<>();
        List<Map<String, Object>> rejectedCustomers = new ArrayList<>();
        List<Map<String, Object>> badRequestedCustomers = new ArrayList<>();
        for( Map<String, Object> kvpairs : jsonMap ) {
            Optional<Customer> customer = accept(kvpairs);
            if(!customer.isEmpty()) {
                if(!customerRepository.existsById(customer.get().getId())) {
                    acceptedCustomers.add(customer.get());
                }
                else {
                    rejectedCustomers.add(kvpairs);
                }
            }
            else {
                badRequestedCustomers.add(kvpairs);
            }
        }
        //
        if(!badRequestedCustomers.isEmpty()) {
            return new ResponseEntity<>( badRequestedCustomers, HttpStatus.BAD_REQUEST );
        }
        //
        if(!rejectedCustomers.isEmpty()) {
            return new ResponseEntity<>( rejectedCustomers, HttpStatus.CONFLICT );
        }
        // save entities if no conflicts occurred
        for(Customer customer: acceptedCustomers) {
            customerRepository.save(customer);
        }
        return new ResponseEntity<>( null, HttpStatus.CREATED );
    }

    /**
     * PUT /customers
     *
     * Update existing customers from JSON data passed as array of JSON objects in the request body.
     * Multiple customers can be updated from multiple JSON objects from the same request.
     *
     * JSON data missing id or with id that are not found are rejected. Rejected JSON objects
     * are returned in the response with error 404 (not found).
     *
     * Status 202 (accepted) is returned with empty array of conflicts when all updates could be
     * performed. Partial acceptance of updates is possible for entire objects only (not single
     * attributes). Error 409 (conflict) is returned for errors other than an object (id) was not
     * found along with the array of rejected objects.
     *
     * @param jsonMap array of maps with raw JSON {@code <key,obj>}-data.
     * @return JSON array with the rejected JSON objects, empty array [] if all updates were accepted.
     */

    /*
     * Swagger API doc annotations:
     */
    @Override
    public ResponseEntity<List<?>> putCustomers(Map<String, Object>[] jsonMap ) {
        System.err.println( "PUT /customers ");
        return new ResponseEntity<>( null, HttpStatus.ACCEPTED ); // status 202
    }

    /**
     * DELETE /customers/{id}
     *
     * Delete existing customer by its id. A missing id or an id that was not found
     * returns error 404 (not found).
     *
     * Status 202 (accepted) is returned with successful completion of the operation.
     *
     * @param id id of object to delete.
     * @return status code: 202 (accepted), 404 (not found).
     */

    /*
     * Swagger API doc annotations:
     */
    @Override
    public ResponseEntity<?> deleteCustomer( long id ) {

        System.err.println( "DELETE /customers/" + id );

        if(id < 0) return new ResponseEntity<List<?>>( HttpStatus.BAD_REQUEST ); // status 400
        if(!customerRepository.existsById(id)) return new ResponseEntity<>( null, HttpStatus.NOT_FOUND ); // status 404
        customerRepository.deleteById(id);
        return new ResponseEntity<>( null, HttpStatus.ACCEPTED ); // status 202
    }

    /*
        Private methods
     */

    private ArrayNode customersAsJSON() {
        //
        ArrayNode arrayNode = objectMapper.createArrayNode();
        //
        Iterable<Customer> customers = customerRepository.findAll();
        if(customers != null) {
            customers.forEach(c -> {
                StringBuffer sb = new StringBuffer();
                c.getContacts().forEach(contact -> sb.append(sb.length() == 0 ? "" : "; ").append(contact));
                arrayNode.add(
                        objectMapper.createObjectNode()
                                .put("name", c.getLastName())
                                .put("first", c.getFirstName())
                                .put("contacts", sb.toString())
                );
            });
        }
        return arrayNode;
    }

    private ArrayNode customerAsJSON(long id) {
        //
        ArrayNode arrayNode = objectMapper.createArrayNode();
        //
        Optional<Customer> customer = customerRepository.findById(id);
        if (!customer.isEmpty()) {
            arrayNode.add(
                    objectMapper.createObjectNode()
                            .put("name", customer.get().getLastName())
                            .put("first", customer.get().getFirstName())
                            .put("contacts", customer.get().toString())
            );
        }
        return arrayNode;
    }

    private Optional<Customer> accept( Map<String, Object> kvpairs ) {
        Customer customer = new Customer();

        if(kvpairs.containsKey("id")) {
            customer.setId(Long.parseLong(kvpairs.get("id").toString()));
        }
        else {
            // find next available id
            long i = 0;
            while(customerRepository.existsById(i)) i++;
            customer.setId(i);
        }

        if(kvpairs.containsKey("first") && kvpairs.containsKey("name")) {
            if(kvpairs.get("first") != null && kvpairs.get("name") != null)
                customer.setName(kvpairs.get("first").toString(), kvpairs.get("name").toString());
        }

        if(kvpairs.containsKey("contacts")) {
            String[] contacts = kvpairs.get("contacts").toString().trim().split("[ ; ][ ;][; ][;]");
            for (String contact : contacts) {
                customer.addContact(contact);
            }
        }

        if(customer == null || customer.getId() < 0 || customer.getName().equals("")) return Optional.empty();
        return Optional.of(customer);
    }
}
