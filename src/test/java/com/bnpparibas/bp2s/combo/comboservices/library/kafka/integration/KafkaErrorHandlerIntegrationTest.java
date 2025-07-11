package com.bnpparibas.bp2s.combo.comboservices.library.kafka.integration;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@SuppressWarnings("squid:S2187") // S2187 = "Test classes should contain test methods"
@Suite
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.bnpparibas.bp2s.combo.comboservices.library.kafka.integration.stepdefs")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
public class KafkaErrorHandlerIntegrationTest {
    // NO SONAR - This class only defines the Cucumber suite.
}
