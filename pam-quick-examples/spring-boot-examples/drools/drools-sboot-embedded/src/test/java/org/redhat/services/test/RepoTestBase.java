package org.redhat.services.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.redhat.services.service.api.AuditService;
import org.redhat.services.util.GeneralUtils;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages={"org.redhat.services.model.entity"})
//@AutoConfigureTestDatabase(replace = Replace.NONE )
//@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(classes = {AuditService.class, GeneralUtils.class, ObjectMapper.class})
@ActiveProfiles("test")
public abstract class RepoTestBase {

}
