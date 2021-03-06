package com.crawljax.core.plugin;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.html.dom.HTMLAnchorElementImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

@Category(BrowserTest.class)
public class OnFireEventFailedPluginTest {

	private CrawljaxController controller;
	private CrawljaxConfiguration config;

	private final AtomicInteger hits = new AtomicInteger();

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("site");

	@Before
	public void setup() throws ConfigurationException {

		CrawlSpecification spec =
		        new CrawlSpecification(SERVER.getSiteUrl() + "crawler/index.html");
		spec.clickDefaultElements();
		spec.clickHiddenAnchors(false);
		config = new CrawljaxConfiguration();

		config.setCrawlSpecification(spec);
		config.addPlugin(new PreStateCrawlingPlugin() {

			@Override
			public void preStateCrawling(CrawlSession session,
			        List<CandidateElement> candidateElement) {
				for (CandidateElement candidate : candidateElement) {
					HTMLAnchorElementImpl impl = (HTMLAnchorElementImpl) candidate.getElement();
					impl.setName("fail");
					impl.setId("eventually");
					impl.setHref("will");
					impl.setTextContent("This");
					candidate.getIdentification().setValue("/HTML[1]/BODY[1]/FAILED[1]/A[1]");
				}
			}
		});
		config.addPlugin(new OnFireEventFailedPlugin() {
			@Override
			public void onFireEventFailed(Eventable eventable, List<Eventable> pathToFailure) {
				hits.incrementAndGet();
			}
		});

		controller = new CrawljaxController(config);
	}

	@Test
	public void testFireEventFaildHasBeenExecuted() throws ConfigurationException,
	        CrawljaxException {
		controller.run();
		assertThat("The FireEventFaild Plugin has been executed the correct amount of times",
		        hits.get(), is(controller.getElementChecker().numberOfExaminedElements()));
	}

	@After
	public void cleanUp() {
		CrawljaxPluginsUtil.loadPlugins(null);
	}

}
