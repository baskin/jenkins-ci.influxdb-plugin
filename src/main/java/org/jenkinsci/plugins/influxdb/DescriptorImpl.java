package org.jenkinsci.plugins.influxdb;

import hudson.model.AbstractProject;
import hudson.model.ModelObject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.CopyOnWriteMap;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import utils.GraphiteValidator;
import utils.MetricsEnum;

import java.util.Iterator;

/**
 * @author joachimrodrigues
 * 
 */
public final class DescriptorImpl extends BuildStepDescriptor<Publisher> implements ModelObject {

	
	private final CopyOnWriteMap<String, Metric> metricsMap = new CopyOnWriteMap.Hash();

	private final CopyOnWriteList<Server> servers = new CopyOnWriteList<Server>();

	private GraphiteValidator validator = new GraphiteValidator();

	
	private String baseQueueName;

	/**
	 * The default constructor.
	 */
	public DescriptorImpl() {
		super(GraphitePublisher.class);
		load();
	}


	/**
	 * @return metrics
	 */
	public Metric[] getMetrics() {
		metricsMap.clear();
		MetricsEnum[] values = MetricsEnum.values();
		Metric metric = null;
		for (int i = 0; i < values.length; i++) {
			metric = new Metric();
			metric.setName(values[i].name());
			metric.setDescription(values[i].toString());
			metricsMap.put(values[i].name(), metric);
		}
		return metricsMap.values().toArray(new Metric[values.length]);
	}

	
	/**
	 * @return servers
	 */
	public Server[] getServers() {
		Iterator<Server> it = servers.iterator();
		int size = 0;
		while (it.hasNext()) {
			it.next();
			size++;
		}
		return servers.toArray(new Server[size]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Descriptor#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "Publish metrics to Graphite Server";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
	 */
	@Override
	public boolean isApplicable(Class<? extends AbstractProject> jobType) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest, net.sf.json.JSONObject)
	 */
	@Override
	public Publisher newInstance(StaplerRequest req, JSONObject formData) {
		GraphitePublisher publisher = new GraphitePublisher();
		req.bindParameters(publisher, "publisherBinding.");
		publisher.getMetrics().addAll(req.bindParametersToList(Metric.class, "metricBinding."));
		return publisher;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Descriptor#configure(org.kohsuke.stapler.StaplerRequest, net.sf.json.JSONObject)
	 */
	@Override
	public boolean configure(StaplerRequest req, JSONObject formData) {
		servers.replaceBy(req.bindParametersToList(Server.class, "serverBinding."));
		baseQueueName = formData.optString("baseQueueName", "");
		save();
		return true;
	}

public String getBaseQueueName(){
		return baseQueueName;
	}


	public GraphiteValidator getValidator() {
	return validator;
}


public void setValidator(GraphiteValidator validator) {
	this.validator = validator;
}


public CopyOnWriteMap<String, Metric> getMetricsMap() {
	return metricsMap;
}


public void setBaseQueueName(String baseQueueName) {
	this.baseQueueName = baseQueueName;
}


	/**
	 * @param ip
	 * @param port
	 * @return form validation of connection status.
	 */
	public FormValidation doTestConnection(@QueryParameter("serverBinding.ip") final String ip,
			@QueryParameter("serverBinding.port") final String port,
			@QueryParameter("serverBinding.protocol") final String protocol) {
		if(protocol.equals("UDP")) {
			return FormValidation.ok("UDP is configured");
		}
		else if(protocol.equals("TCP")) {
			if (!validator.isIpPresent(ip) || !validator.isPortPresent(port)
					|| !validator.isListening(ip, Integer.parseInt(port))) {
				return FormValidation.error("Server is not listening... Or ip:port are not correctly filled");
			}

			return FormValidation.ok("Server is listening");
		} else {
			return FormValidation.ok("Unknown protocol");
		}
	}

	/**
	 * @param value
	 * @return  form validation of ip status.
	 */
	public FormValidation doCheckIp(@QueryParameter final String value) {
		if (!validator.isIpPresent(value)) {
			return FormValidation.error("Please set a ip");
		}
		if (!validator.validateIpFormat(value)) {
			return FormValidation.error("Please check the IP format");
		}

		return FormValidation.ok("IP is correctly configured");
	}

	/**
	 * @param value
	 * @return  form validation of description
	 */
	public FormValidation doCheckDescription(@QueryParameter final String value) {
		if (!validator.isDescriptionPresent(value)) {
			return FormValidation.error("Please set a description");
		}
		if (validator.isDescriptionTooLong(value)) {
			return FormValidation.error("Description is limited to 100 characters");
		}

		return FormValidation.ok("Description is correctly configured");
	}

	/**
	 * @param value
	 * @return  form validation of port.
	 */
	public FormValidation doCheckPort(@QueryParameter final String value) {
		if (!validator.isPortPresent(value)) {
			return FormValidation.error("Please set a port");
		}

		if (!validator.validatePortFormat(value)) {
			return FormValidation.error("Please check the port format");
		}

		return FormValidation.ok("Port is correctly configured");
	}
	
	/**
	 * 
	 * @param value
	 * @return  form validation of base queue name
	 */
	public FormValidation doCheckBaseQueueName(@QueryParameter final String value) {
	    if(!validator.isBaseQueueNamePresent(value)){
	        return FormValidation.ok();
	    }
	    
	    if(!validator.validateBaseQueueName(value)){
	        return FormValidation.error("Please ");
	    }
	    
	    return FormValidation.ok("Base queue name is correctly Configured");
	    
	}
}
