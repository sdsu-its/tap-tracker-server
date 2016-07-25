package edu.sdsu.its.taptracker_tests;

import edu.sdsu.its.taptracker.Param;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test Key Server Environment Variable configuration and Test Connection to Server.
 *
 * @author Tom Paulus
 *         Created on 7/13/16.
 */
public class KeyServ {
    private final static Logger LOGGER = Logger.getLogger(KeyServ.class);

    /**
     * Check that the environment variables that are used by the Key Server are set.
     */
    @Test
    public void checkENV() throws Exception {
        final String path = System.getenv("KSPATH");
        final String key = System.getenv("KSKEY");
        final String name = System.getenv("TAP_APP");

        LOGGER.debug("ENV.KSPATH=" + path);
        LOGGER.debug("ENV.KSKEY=" + key);
        LOGGER.debug("ENV.TAP_APP=" + name);

        assertTrue("Empty KS URL", isDefined(path));
        assertTrue("Empty KS API Key", isDefined(key));
        assertTrue("Empty App Name", isDefined(name));
    }

    /**
     * Perform a self-test of the connection to the server.
     * Validity of the app-name and api-key are NOT checked.
     */
    @Test
    public void checkConnection() throws Exception {
        assertTrue(Param.testConnection());
    }

    @Test
    public void checkParams() throws Exception {
        assertTrue("\"DB URL\" Not Defined", isDefined(Param.getParam("db-url")));
        assertTrue("\"DB User\" Not Defined", isDefined(Param.getParam("db-user")));
        assertTrue("\"DB Password\" Not Defined", isDefined(Param.getParam("db-password")));

        assertTrue("\"Project Token\" Not Defined", isDefined(Param.getParam("project_token")));
        assertTrue("\"Token Cypher\" Not Defined", isDefined(Param.getParam("token_cypher")));
        assertTrue("\"Token TTL\" Not Defined", isDefined(Param.getParam("token_ttl")));
    }

    private boolean isDefined(String s) {
        return s != null && s.length() > 0;
    }
}
