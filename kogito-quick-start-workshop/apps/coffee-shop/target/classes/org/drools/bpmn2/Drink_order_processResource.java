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
import org.drools.bpmn2.Drink_order_processModel;

@Path("/drink_order_process")
@javax.enterprise.context.ApplicationScoped()
public class Drink_order_processResource {

    @javax.inject.Inject()
    @javax.inject.Named("drink_order_process")
    Process<Drink_order_processModel> process;

    @javax.inject.Inject()
    Application application;

    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Drink_order_processModel createResource_drink_order_process(@Context HttpHeaders httpHeaders, Drink_order_processModel resource) {
        if (resource == null) {
            resource = new Drink_order_processModel();
        }
        final Drink_order_processModel value = resource;
        return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            ProcessInstance<Drink_order_processModel> pi = process.createInstance(value);
            String startFromNode = httpHeaders.getHeaderString("X-KOGITO-StartFromNode");
            if (startFromNode != null) {
                pi.startFrom(startFromNode);
            } else {
                pi.start();
            }
            return getModel(pi);
        });
    }

    @GET()
    @Produces(MediaType.APPLICATION_JSON)
    public List<Drink_order_processModel> getResources_drink_order_process() {
        return process.instances().values().stream().map(ProcessInstance::variables).collect(Collectors.toList());
    }

    @GET()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_processModel getResource_drink_order_process(@PathParam("id") String id) {
        return process.instances().findById(id).map(ProcessInstance::variables).orElse(null);
    }

    @DELETE()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_processModel deleteResource_drink_order_process(@PathParam("id") final String id) {
        return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
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
    public Drink_order_processModel updateModel_drink_order_process(@PathParam("id") String id, Drink_order_processModel resource) {
        return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
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
    public Map<String, String> getTasks_drink_order_process(@PathParam("id") String id, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        return process.instances().findById(id).map(pi -> pi.workItems(policies(user, groups))).map(l -> l.stream().collect(Collectors.toMap(WorkItem::getId, WorkItem::getName))).orElse(null);
    }

    protected Drink_order_processModel getModel(ProcessInstance<Drink_order_processModel> pi) {
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
    @Path("/{id}/payment_type_change/{workItemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_processModel completeTask_3(@PathParam("id") final String id, @PathParam("workItemId") final String workItemId, @QueryParam("phase") @DefaultValue("complete") final String phase, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups, final Drink_order_process_3_TaskOutput model) {
        try {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
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
    @Path("/{id}/payment_type_change/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_process_3_TaskInput getTask_3(@PathParam("id") String id, @PathParam("workItemId") String workItemId, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        try {
            ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
            if (pi == null) {
                return null;
            } else {
                WorkItem workItem = pi.workItem(workItemId, policies(user, groups));
                if (workItem == null) {
                    return null;
                }
                return Drink_order_process_3_TaskInput.fromMap(workItem.getId(), workItem.getName(), workItem.getParameters());
            }
        } catch (WorkItemNotFoundException e) {
            return null;
        }
    }

    @DELETE()
    @Path("/{id}/payment_type_change/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_processModel abortTask_3(@PathParam("id") final String id, @PathParam("workItemId") final String workItemId, @QueryParam("phase") @DefaultValue("abort") final String phase, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        try {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
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
    @Path("/{id}/make_payment/{workItemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_processModel completeTask_7(@PathParam("id") final String id, @PathParam("workItemId") final String workItemId, @QueryParam("phase") @DefaultValue("complete") final String phase, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups, final Drink_order_process_7_TaskOutput model) {
        try {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
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
    @Path("/{id}/make_payment/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_process_7_TaskInput getTask_7(@PathParam("id") String id, @PathParam("workItemId") String workItemId, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        try {
            ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
            if (pi == null) {
                return null;
            } else {
                WorkItem workItem = pi.workItem(workItemId, policies(user, groups));
                if (workItem == null) {
                    return null;
                }
                return Drink_order_process_7_TaskInput.fromMap(workItem.getId(), workItem.getName(), workItem.getParameters());
            }
        } catch (WorkItemNotFoundException e) {
            return null;
        }
    }

    @DELETE()
    @Path("/{id}/make_payment/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_processModel abortTask_7(@PathParam("id") final String id, @PathParam("workItemId") final String workItemId, @QueryParam("phase") @DefaultValue("abort") final String phase, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        try {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
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
    @Path("/{id}/place_order/{workItemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_processModel completeTask_9(@PathParam("id") final String id, @PathParam("workItemId") final String workItemId, @QueryParam("phase") @DefaultValue("complete") final String phase, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups, final Drink_order_process_9_TaskOutput model) {
        try {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
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
    @Path("/{id}/place_order/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_process_9_TaskInput getTask_9(@PathParam("id") String id, @PathParam("workItemId") String workItemId, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        try {
            ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
            if (pi == null) {
                return null;
            } else {
                WorkItem workItem = pi.workItem(workItemId, policies(user, groups));
                if (workItem == null) {
                    return null;
                }
                return Drink_order_process_9_TaskInput.fromMap(workItem.getId(), workItem.getName(), workItem.getParameters());
            }
        } catch (WorkItemNotFoundException e) {
            return null;
        }
    }

    @DELETE()
    @Path("/{id}/place_order/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Drink_order_processModel abortTask_9(@PathParam("id") final String id, @PathParam("workItemId") final String workItemId, @QueryParam("phase") @DefaultValue("abort") final String phase, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        try {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<Drink_order_processModel> pi = process.instances().findById(id).orElse(null);
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
