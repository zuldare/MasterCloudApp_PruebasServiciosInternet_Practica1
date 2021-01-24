import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectPackages({"es.urjc.code.daw.library.book"})
@IncludeTags("E2E-Tests")
public class AllE2ETest {
}
