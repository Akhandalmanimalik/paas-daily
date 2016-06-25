package com.getusroi.paas.rest.service;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.paas.dao.AclDAO;
import com.getusroi.paas.dao.DataBaseOperationFailedException;
import com.getusroi.paas.rest.RestServiceHelper;
import com.getusroi.paas.rest.service.exception.PAASNetworkServiceException;
import com.getusroi.paas.sdn.service.impl.SDNServiceImplException;
import com.getusroi.paas.vo.ACL;
import com.getusroi.paas.vo.InOutBoundRule;
import com.google.gson.Gson;


@Path("/aclService")
public class ACLServices {
	 static final Logger LOGGER = LoggerFactory.getLogger(ACLServices.class);
	 static final String TENANT="tenant";
	 
	 AclDAO aclDAO = null;
	 InOutBoundRule inOutBoundRule = null;
			 
	 @GET
	 @Path("/getAllACL")
	 @Produces(MediaType.APPLICATION_JSON)
	 public String getAllACL(@Context HttpServletRequest req) throws DataBaseOperationFailedException{
			LOGGER.debug("Inside (.)  getAllACL of ACLServices ");
			aclDAO = new AclDAO();
			String aclInJsonString = null;
 			try {
				HttpSession session = req.getSession(true);
				List<ACL> aclList = aclDAO.getAllACL((int)session.getAttribute("id"));
				Gson gson = new Gson();
				aclInJsonString = gson.toJson(aclList);
				LOGGER.debug(""+aclInJsonString);
				}
			 catch (Exception e) {
					e.printStackTrace();
				}
			return aclInJsonString;
		}//end of method getAllACL
	  
	 
	 @POST
	 @Path("/addACLRule")
	 @Consumes(MediaType.APPLICATION_JSON)
	 @Produces(MediaType.TEXT_PLAIN)
	 public String addACLRule(String aclData,@Context HttpServletRequest req) throws SDNServiceImplException, DataBaseOperationFailedException, PAASNetworkServiceException{
		 LOGGER.debug(".addACLRule method of ACLServices");
		 ObjectMapper mapper = new ObjectMapper();
		  aclDAO=new AclDAO();
		 /*SDNInterface sdnService = new SDNServiceWrapperImpl();
		 boolean flowFlag=false;*/
		 try {
			  
			ACL acl = mapper.readValue(aclData, ACL.class);			
//			flowFlag = sdnService.installFlow(acl.getAclName(), acl.getSourceIp(), acl.getDestinationIp(),PAASConstant.ACL_PASS_ACTION_KEY);
			HttpSession session = req.getSession(true);
			if(acl != null){
				acl.setTenantId((int)session.getAttribute("id"));
				aclDAO.insertACL(acl);
				return "Success";
			}
			
		} catch (IOException e) {
			LOGGER.error("Error in reading data : "+aclData+" using object mapper in addACLRule");
			throw new PAASNetworkServiceException("Error in reading data : "+aclData+" using object mapper in addACLRule");
		}
		 return "failed";
	 }//end of method addACLRule
	 
	 @GET
	 @Path("/getAllACLNames")
	 @Produces(MediaType.APPLICATION_JSON)
	 public String getAllACLNames() throws DataBaseOperationFailedException{
		 LOGGER.debug(".getAllACL method of ACLServices");
		 aclDAO=new AclDAO();
		 List<String> aclList=aclDAO.getAllACLNames();
		 Gson gson=new Gson();
		 String aclInJsonString=gson.toJson(aclList);
		 return aclInJsonString;
	 }//end of method getAllACLNames
	 
	 	@GET
		@Path("/deleteACLByNameUsingTenantId/{aclName}")
		@Produces(MediaType.TEXT_PLAIN)
		public String deleteACLByNameUsingTenantId(@PathParam("aclName") String aclName,@Context HttpServletRequest req)
				throws DataBaseOperationFailedException {
	 		LOGGER.debug(".deleteAclByName method of ACLServices");
	 		LOGGER.debug("Name is"+aclName);
			 
			aclDAO = new AclDAO();
			RestServiceHelper restServiceHelper = new RestServiceHelper();
			HttpSession session = req.getSession(true);
			
			int tenant_id = restServiceHelper.convertStringToInteger(session
					.getAttribute("id") + "");
			
			aclDAO.deleteACLByName(aclName,tenant_id);
			return "acl with name : " + aclName + " is delete successfully";
		}// end 
	 
	 	
		/**
		 * To chckAcl name exist or not
		 * @param aclName
		 * @param req
		 * @return
		 * @throws DataBaseOperationFailedException
		 */
		@GET
		@Path("/checkAcl/{aclName}")
		@Produces(MediaType.TEXT_PLAIN)
		public String aclValidation(@PathParam("aclName") String aclName,
				@Context HttpServletRequest req)
				throws DataBaseOperationFailedException {
			LOGGER.debug(" Inside (.) aclValidation of ACLServices");
			HttpSession session = req.getSession(true);
			aclDAO = new AclDAO();
			int id = aclDAO.getACLIdByACLNames(aclName,
					(int) session.getAttribute("id"));
			if (id > 0)
				return "success";
			else
				return "failure";
		}// end of method aClByName validation
		
		
		
		@POST
		 @Path("/addInOutBoundRule")
		 @Consumes(MediaType.APPLICATION_JSON)
		 public void addInOutBoundRule(String aclData,@Context HttpServletRequest req) throws SDNServiceImplException, DataBaseOperationFailedException, PAASNetworkServiceException{
			 LOGGER.debug("Inside (.) addACLRule of ACLServices");
			 ObjectMapper mapper = new ObjectMapper();
			 aclDAO =new AclDAO();
			 try {
				 inOutBoundRule = mapper.readValue(aclData, InOutBoundRule.class);			
				if(inOutBoundRule != null)
					aclDAO.insertInOutBoundRule(inOutBoundRule);
				
			} catch (IOException e) {
				LOGGER.error("Error in reading data : "+aclData+" using object mapper in addACLRule");
				throw new PAASNetworkServiceException("Error in reading data : "+aclData+" using object mapper in addACLRule");
			}
		 }//end of method addACLRule
		
		
		/**
		 * To get all Inoutbound rule From db by acl id
		 * @param aclName
		 * @param req
		 * @return
		 * @throws DataBaseOperationFailedException
		 */
		@GET
		@Path("/getAllInOutBoundRuleByAclId/{aclId}")
		@Produces(MediaType.APPLICATION_JSON)
		public String getAllInOutBoundRuleByAclId(@PathParam("aclId") int aclId,@Context HttpServletRequest req)
				throws DataBaseOperationFailedException {
			LOGGER.debug("Inside (.) getAllInOutBoundRuleByAclId of ACLServices "+aclId);
			aclDAO = new AclDAO();
			String aclInJsonString = null;
			try {
				LOGGER.debug("BEFORE SESSION");
				HttpSession session = req.getSession(true);
				List<InOutBoundRule> aclList = aclDAO.getAllInOutBoundRules(aclId, (int) session.getAttribute("id"));
				Gson gson = new Gson();
				aclInJsonString = gson.toJson(aclList);
				LOGGER.debug("" + aclInJsonString);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return aclInJsonString;
		}// end of method getAllACL
		
		
		@GET
		@Path("/deleteInOutBoundRuleById/{id}")
		@Produces(MediaType.TEXT_PLAIN)
		public String deleteInOutBoundRuleById(@PathParam("id") int id,@Context HttpServletRequest req)
				throws DataBaseOperationFailedException {
	 		LOGGER.debug(".deleteAclByName method of PAASNetworkService id "+id);
			aclDAO = new AclDAO();
			aclDAO.deleteInOutBoundRuleById(id);
			return "acl with name : " + id + " is delete successfully";
		}// end 
		
}