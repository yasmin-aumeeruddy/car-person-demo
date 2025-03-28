
/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/

package io.openliberty.demo.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PersonClient {

    // Constants for building URI to the person service.
    private final String PEOPLE = "/people/getPerson/";
    private final String PROTOCOL = "http";

    private String url;
    private Builder clientBuilder;

    private static final Logger julLogger = Logger.getLogger("jul-logger");
    public void init(String hostname, int port, UUID personId) {
        this.initHelper(hostname, port, personId);
    }

    // Helper method to set the attributes.
    private void initHelper(String hostname, int port, UUID personId) {
        this.url = buildUrl(PROTOCOL, hostname, port, PEOPLE + personId);
        julLogger.log(Level.INFO, "Building client for url " + url);
        this.clientBuilder = buildClientBuilder(this.url);
    }

    // Wrapper function that gets properties
    public String getPerson() {
        return getPersonHelper(this.clientBuilder);
    }

    /**
     * Builds the URI string to the system service for a particular host.
     * @param protocol
     *          - http or https.
     * @param host
     *          - name of host.
     * @param port
     *          - port number.
     * @param path
     *          - Note that the path needs to start with a slash!!!
     * @return String representation of the URI to the system properties service.
     */
    
    protected String buildUrl(String protocol, String host, int port, String path) {
        try {
            URI uri = new URI(protocol, null, host, port, path, null, null);
            return uri.toString();
        } catch (Exception e) {
            julLogger.log(Level.SEVERE, "URISyntaxException");
            return null;
        }
    }

    // Method that creates the client builder
    protected Builder buildClientBuilder(String urlString) {
        try {
            Client client = ClientBuilder.newClient();
            Builder builder = client.target(urlString).request();
            return builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        } catch (Exception e) {
            julLogger.log(Level.SEVERE, "ClientBuilderException");
            return null;
        }
    }

    // Helper method that processes the request
    protected String getPersonHelper(Builder builder) {
        try {
            Response response = builder.get();
            System.out.println(response);
            if (response.getStatus() == Status.OK.getStatusCode()) {
                return response.readEntity(String.class);
            } else {
                julLogger.log(Level.SEVERE, "Response status is not okay");
            }
        } catch (RuntimeException e) {
            julLogger.log(Level.SEVERE, "Runtime exception: " + e.getMessage());
        } catch (Exception e) {
            julLogger.log(Level.SEVERE, "Exception thrown while invoking the request.");
        }
        return null;
    }
}
