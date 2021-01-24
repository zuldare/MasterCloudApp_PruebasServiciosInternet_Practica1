import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectPackages({"es.urjc.code.daw.library"})
@SelectClasses({AllE2ETestSuite.class, AllUnitTestSuite.class})
public class AllTestsSuite {
}
