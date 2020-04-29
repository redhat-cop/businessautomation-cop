package org.drools.bpmn2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.kie.api.runtime.process.WorkItemNotFoundException;
import org.kie.kogito.Application;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceExecutionException;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.workitem.Policy;
import org.drools.bpmn2.Barista_processModel;

@Path("/barista_process")
@javax.enterprise.context.ApplicationScoped()
public class Barista_processResource {

    @javax.inject.Inject()
    @javax.inject.Named("barista_process")
    Process<Barista_processModel> process;

    @javax.inject.Inject()
    Application application;

    @GET()
    @Produces(MediaType.APPLICATION_JSON)
    public List<Barista_processModel> getResources_barista_process() {
        return process.instances().values().stream().map(ProcessInstance::variables).collect(Collectors.toList());
    }

    @GET()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Barista_processModel getResource_barista_process(@PathParam("id") String id) {
        return process.instances().findById(id).map(ProcessInstance::variables).orElse(null);
    }

    @DELETE()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Barista_processModel deleteResource_barista_process(@PathParam("id") final String id) {
        return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            ProcessInstance<Barista_processModel> pi = process.instances().findById(id).orElse(null);
            if (pi == null) {
                return null;
            } else {
                pi.abort();
                return getModel(pi);
            }
        });
    }

    @POST()
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Barista_processModel updateModel_barista_process(@PathParam("id") String id, Barista_processModel resource) {
        return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            ProcessInstance<Barista_processModel> pi = process.instances().findById(id).orElse(null);
            if (pi == null) {
                return null;
            } else {
                pi.updateVariables(resource);
                return pi.variables();
            }
        });
    }

    @GET()
    @Path("/{id}/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getTasks_barista_process(@PathParam("id") String id, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        return process.instances().findById(id).map(pi -> pi.workItems(policies(user, groups))).map(l -> l.stream().collect(Collectors.toMap(WorkItem::getId, WorkItem::getName))).orElse(null);
    }

    protected Barista_processModel getModel(ProcessInstance<Barista_processModel> pi) {
        if (pi.status() == ProcessInstance.STATE_ERROR && pi.error().isPresent()) {
            throw new ProcessInstanceExecutionException(pi.id(), pi.error().get().failedNodeId(), pi.error().get().errorMessage());
        }
        return pi.variables();
    }

    protected Policy[] policies(String user, List<String> groups) {
        if (user == null) {
            return new Policy[0];
        }
        org.kie.kogito.auth.IdentityProvider identity = null;
        if (user != null) {
            identity = new org.kie.kogito.services.identity.StaticIdentityProvider(user, groups);
        }
        return new Policy[] { SecurityPolicy.of(identity) };
    }

    @POST()
    @Path("/{id}/collect_drink/{workItemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Barista_processModel completeTask_2(@PathParam("id") final String id, @PathParam("workItemId") final String workItemId, @QueryParam("phase") @DefaultValue("complete") final String phase, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups, final Barista_process_2_TaskOutput model) {
        try {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<Barista_processModel> pi = process.instances().findById(id).orElse(null);
                if (pi == null) {
                    return null;
                } else {
                    org.kie.kogito.auth.IdentityProvider identity = null;
                    if (user != null) {
                        identity = new org.kie.kogito.services.identity.StaticIdentityProvider(user, groups);
                    }
                    org.jbpm.process.instance.impl.humantask.HumanTaskTransition transition = new org.jbpm.process.instance.impl.humantask.HumanTaskTransition(phase, model.toMap(), identity);
                    pi.transitionWorkItem(workItemId, transition);
                    return getModel(pi);
                }
            });
        } catch (WorkItemNotFoundException e) {
            return null;
        }
    }

    @GET()
    @Path("/{id}/collect_drink/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Barista_process_2_TaskInput getTask_2(@PathParam("id") String id, @PathParam("workItemId") String workItemId, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        try {
            ProcessInstance<Barista_processModel> pi = process.instances().findById(id).orElse(null);
            if (pi == null) {
                return null;
            } else {
                WorkItem workItem = pi.workItem(workItemId, policies(user, groups));
                if (workItem == null) {
                    return null;
                }
                return Barista_process_2_TaskInput.fromMap(workItem.getId(), workItem.getName(), workItem.getParameters());
            }
        } catch (WorkItemNotFoundException e) {
            return null;
        }
    }

    @DELETE()
    @Path("/{id}/collect_drink/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Barista_processModel abortTask_2(@PathParam("id") final String id, @PathParam("workItemId") final String workItemId, @QueryParam("phase") @DefaultValue("abort") final String phase, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        try {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<Barista_processModel> pi = process.instances().findById(id).orElse(null);
                if (pi == null) {
                    return null;
                } else {
                    org.kie.kogito.auth.IdentityProvider identity = null;
                    if (user != null) {
                        identity = new org.kie.kogito.services.identity.StaticIdentityProvider(user, groups);
                    }
                    org.jbpm.process.instance.impl.humantask.HumanTaskTransition transition = new org.jbpm.process.instance.impl.humantask.HumanTaskTransition(phase, null, identity);
                    pi.transitionWorkItem(workItemId, transition);
                    return getModel(pi);
                }
            });
        } catch (WorkItemNotFoundException e) {
            return null;
        }
    }

    @POST()
    @Path("/{id}/prepare_drink/{workItemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Barista_processModel completeTask_3(@PathParam("id") final String id, @PathParam("workItemId") final String workItemId, @QueryParam("phase") @DefaultValue("complete") final String phase, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups, final Barista_process_3_TaskOutput model) {
        try {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<Barista_processModel> pi = process.instances().findById(id).orElse(null);
                if (pi == null) {
                    return null;
                } else {
                    org.kie.kogito.auth.IdentityProvider identity = null;
                    if (user != null) {
                        identity = new org.kie.kogito.services.identity.StaticIdentityProvider(user, groups);
                    }
                    org.jbpm.process.instance.impl.humantask.HumanTaskTransition transition = new org.jbpm.process.instance.impl.humantask.HumanTaskTransition(phase, model.toMap(), identity);
                    pi.transitionWorkItem(workItemId, transition);
                    return getModel(pi);
                }
            });
        } catch (WorkItemNotFoundException e) {
            return null;
        }
    }

    @GET()
    @Path("/{id}/prepare_drink/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Barista_process_3_TaskInput getTask_3(@PathParam("id") String id, @PathParam("workItemId") String workItemId, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        try {
            ProcessInstance<Barista_processModel> pi = process.instances().findById(id).orElse(null);
            if (pi == null) {
                return null;
            } else {
                WorkItem workItem = pi.workItem(workItemId, policies(user, groups));
                if (workItem == null) {
                    return null;
                }
                return Barista_process_3_TaskInput.fromMap(workItem.getId(), workItem.getName(), workItem.getParameters());
            }
        } catch (WorkItemNotFoundException e) {
            return null;
        }
    }

    @DELETE()
    @Path("/{id}/prepare_drink/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Barista_processModel abortTask_3(@PathParam("id") final String id, @PathParam("workItemId") final String workItemId, @QueryParam("phase") @DefaultValue("abort") final String phase, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        try {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<Barista_processModel> pi = process.instances().findById(id).orElse(null);
                if (pi == null) {
                    return null;
                } else {
                    org.kie.kogito.auth.IdentityProvider identity = null;
                    if (user != null) {
                        identity = new org.kie.kogito.services.identity.StaticIdentityProvider(user, groups);
                    }
                    org.jbpm.process.instance.impl.humantask.HumanTaskTransition transition = new org.jbpm.process.instance.impl.humantask.HumanTaskTransition(phase, null, identity);
                    pi.transitionWorkItem(workItemId, transition);
                    return getModel(pi);
                }
            });
        } catch (WorkItemNotFoundException e) {
            return null;
        }
    }
}
