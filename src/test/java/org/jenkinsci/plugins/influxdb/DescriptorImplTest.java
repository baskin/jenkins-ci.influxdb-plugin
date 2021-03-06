package org.jenkinsci.plugins.influxdb;

import hudson.model.AbstractProject;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.MockQueueItemAuthenticator;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by jrajala on 2.5.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class DescriptorImplTest {

    public static final boolean DONT_LOAD_CONFIGURATION = false;

    public static final String VALID_DATABASE_NAME = "database";
    public static final String NONPRESET_DATABASE_NAME = "";
    public static final String VALID_PORT = "8086";
    public static final String NONPRESENT_PORT = "";
    public static final String INVALID_PORT = "70000";
    public static final String VALID_DESCRIPTION = "description";
    public static final String NONPRESENT_DESCRIPTION = "";
    public static final String TOO_LONG_DESCRIPTION = "too long description";
    public static final String VALID_HOSTNAME = "host";
    public static final String INVALID_HOSTNAME = "";

    private DescriptorImpl descriptor;
    private InfluxDbValidator validator;
    private StaplerRequest request;

    @Before
    public void setupTestCase() {
        descriptor = new DescriptorImpl();
        validator = Mockito.mock(InfluxDbValidator.class);
        descriptor.setValidator(validator);
        request = Mockito.mock(StaplerRequest.class);
    }

    @Test
    public void callingNewInstanceShouldCreateNewInstanceOfInfluxDbPublisher() {
        Publisher publisher1 = descriptor.newInstance(request, null);
        Publisher publisher2 = descriptor.newInstance(request, null);
        Assert.assertNotSame(publisher1, publisher2);
        Assert.assertSame(InfluxDbPublisher.class,  publisher1.getClass());
    }

    @Test
    public void shouldGiveCorrectDisplayName() {
        Assert.assertEquals(DescriptorImpl.DISPLAY_NAME, descriptor.getDisplayName());
    }

    @Test
    public void shouldBeApplicableForAnyProjectType() {
        Assert.assertTrue(descriptor.isApplicable(AbstractProject.class));
    }

    @Test
    public void validatorShouldBeSameThatWasSet() {
        Assert.assertSame(validator, descriptor.getValidator());
    }

    @Test
    public void checkingDatabaseNameShouldUseValidatorAndPassWithValidResult() {
        Mockito.when(validator.isDatabaseNamePresent(VALID_DATABASE_NAME)).thenReturn(true);
        FormValidation result = descriptor.doCheckDatabaseName(VALID_DATABASE_NAME);
        Mockito.verify(validator, Mockito.times(1)).isDatabaseNamePresent(Matchers.eq(VALID_DATABASE_NAME));
        Assert.assertTrue(result.kind.equals(FormValidation.Kind.OK));
    }

    @Test
    public void checkingDatabaseNameShouldGiveValidationErrorIfValidatorReturnFailure() {
        Mockito.when(validator.isDescriptionPresent(NONPRESET_DATABASE_NAME)).thenReturn(false);
        FormValidation result = descriptor.doCheckDatabaseName(NONPRESET_DATABASE_NAME);
        Assert.assertTrue(result.kind.equals(FormValidation.Kind.ERROR));
    }

    @Test
    public void checkingPortShouldUseValidatorForCheckingExistenceAndValidityOfPortAndPassOnValidResult() {

        Mockito.when(validator.isPortPresent(VALID_PORT)).thenReturn(true);
        Mockito.when(validator.validatePortFormat(VALID_PORT)).thenReturn(true);

        FormValidation result = descriptor.doCheckPort(VALID_PORT);

        Mockito.verify(validator, Mockito.times(1)).isPortPresent(Matchers.eq(VALID_PORT));
        Mockito.verify(validator, Mockito.times(1)).validatePortFormat(Matchers.eq(VALID_PORT));
        Assert.assertTrue(result.kind.equals(FormValidation.Kind.OK));
    }

    @Test
    public void checkingDatabaseNameShouldGiveValidationErrorIfValidatorReturnFailureForNonPresentPort() {
        Mockito.when(validator.isPortPresent(NONPRESENT_PORT)).thenReturn(false);

        FormValidation result = descriptor.doCheckPort(NONPRESENT_PORT);

        Assert.assertTrue(result.kind.equals(FormValidation.Kind.ERROR));
    }

    @Test
    public void checkingDatabaseNameShouldGiveValidationErrorIfValidatorReturnFailureForInvalidPort() {
        Mockito.when(validator.isPortPresent(INVALID_PORT)).thenReturn(true);
        Mockito.when(validator.validatePortFormat(INVALID_PORT)).thenReturn(false);

        FormValidation result = descriptor.doCheckPort(INVALID_PORT);

        Assert.assertTrue(result.kind.equals(FormValidation.Kind.ERROR));
    }

    @Test
    public void checkingDescriptorShouldUseValidatorAndPassOnValidResult () {
        Mockito.when(validator.isDescriptionPresent(VALID_DESCRIPTION)).thenReturn(true);
        Mockito.when(validator.isDescriptionTooLong(VALID_DESCRIPTION)).thenReturn(false);

        FormValidation result = descriptor.doCheckDescription(VALID_DESCRIPTION);

        Mockito.verify(validator, Mockito.times(1)).isDescriptionPresent(VALID_DESCRIPTION);
        Mockito.verify(validator, Mockito.times(1)).isDescriptionTooLong(VALID_DESCRIPTION);
        Assert.assertTrue(result.kind.equals(FormValidation.Kind.OK));

    }

    @Test
    public void checkingDescriptorShouldUseValidatorAndFailOnNonExistentDescrition() {
        Mockito.when(validator.isDescriptionPresent(NONPRESENT_DESCRIPTION)).thenReturn(false);
        Mockito.when(validator.isDescriptionTooLong(NONPRESENT_DESCRIPTION)).thenReturn(false);

        FormValidation result = descriptor.doCheckDescription(NONPRESENT_DESCRIPTION);

        Mockito.verify(validator, Mockito.times(1)).isDescriptionPresent(NONPRESENT_DESCRIPTION);
        Mockito.verify(validator, Mockito.times(0)).isDescriptionTooLong(NONPRESENT_DESCRIPTION);
        Assert.assertTrue(result.kind.equals(FormValidation.Kind.ERROR));

    }

    @Test
    public void checkingDescriptorShouldUseValidatorAndFailOnTooLongtDescrition() {
        Mockito.when(validator.isDescriptionPresent(TOO_LONG_DESCRIPTION)).thenReturn(true);
        Mockito.when(validator.isDescriptionTooLong(TOO_LONG_DESCRIPTION)).thenReturn(true);

        FormValidation result = descriptor.doCheckDescription(TOO_LONG_DESCRIPTION);

        Mockito.verify(validator, Mockito.times(1)).isDescriptionPresent(TOO_LONG_DESCRIPTION);
        Mockito.verify(validator, Mockito.times(1)).isDescriptionTooLong(TOO_LONG_DESCRIPTION);
        Assert.assertTrue(result.kind.equals(FormValidation.Kind.ERROR));

    }

    @Test
    public void checkingHostShouldUseValidatorAndPassOnValidResult() {
        Mockito.when(validator.isHostPresent(VALID_HOSTNAME)).thenReturn(true);

        FormValidation result = descriptor.doCheckHost(VALID_HOSTNAME);

        Mockito.verify(validator, Mockito.times(1)).isHostPresent(VALID_HOSTNAME);
        Assert.assertTrue(result.kind.equals(FormValidation.Kind.OK));
    }

    @Test
    public void chekcingHostShouldUseValidatorAndFailOnInvalidResult() {
        Mockito.when(validator.isHostPresent(INVALID_HOSTNAME)).thenReturn(false);

        FormValidation result = descriptor.doCheckHost(INVALID_HOSTNAME);

        Mockito.verify(validator, Mockito.times(1)).isHostPresent(INVALID_HOSTNAME);
        Assert.assertTrue(result.kind.equals(FormValidation.Kind.ERROR));

    }

}
