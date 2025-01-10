package org.apereo.cas.multitenancy;

import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTenantsManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Web")
@SpringBootTest(classes = BaseMultitenancyTests.SharedTestConfiguration.class)
@TestPropertySource(properties = "cas.multitenancy.json.location=classpath:/tenants.json")
@ExtendWith(CasTestExtension.class)
class DefaultTenantsManagerTests {

    @Autowired
    @Qualifier(TenantsManager.BEAN_NAME)
    private TenantsManager tenantsManager;

    @Test
    void verifyOperation() {
        val definition = tenantsManager.findTenant("b9584c42");
        assertTrue(definition.isPresent());
    }
}