package edu.rit.csci759.smartblind;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
* Demonstration of the JSON-RPC 2.0 Server framework usage. The request
* handlers are implemented as static nested classes for convenience, but in 
* real life applications may be defined as regular classes within their old 
* source files.
*
* @author Vladimir Dzhuvinov
* @version 2011-03-05
*/

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.RuleBlock;
import net.sourceforge.jFuzzyLogic.rule.RuleExpression;
import net.sourceforge.jFuzzyLogic.rule.RuleTerm;
import net.sourceforge.jFuzzyLogic.ruleConnectionMethod.RuleConnectionMethodAndMin;
import net.sourceforge.jFuzzyLogic.ruleConnectionMethod.RuleConnectionMethodOrMax;

/**
 * This class basically handles the JSON requests.
 * 
 * @author Kapil Dole
 *
 */
public class JsonHandler {

	// Implements a handler for "viewRule", "addRule", "editRule" and
	// "deleteRule" JSON-RPC methods
	// that returns status of the operation.
	public static class RuleHandler implements RequestHandler {

		// Reports the method names of the handled requests
		public String[] handledRequests() {

			return new String[] { "viewRule", "addRule", "deleteRule", "editRule" };
		}

		// Processes the requests based on the method name.
		public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {

			RuleBlock rblock = PiClient.rblock;
			if (req.getMethod().equals("viewRule")) {
				int ruleCounter = 1;
				Map<String, String> rulesMap = new HashMap<String, String>();
				System.out.println("Rules requested: ");
				for (Rule rule : rblock.getRules()) {
					rulesMap.put("" + ruleCounter, "" + rule);
					ruleCounter++;
					System.out.println(rule);
				}
				return new JSONRPC2Response(rulesMap, req.getID());
			} else if (req.getMethod().equals("addRule")) {
				Rule rule = new Rule("" + (rblock.getRules().size() + 1), rblock);
				Map<String, Object> newRule = req.getNamedParams();
				RuleTerm term1 = new RuleTerm(rblock.getVariable("temperature"), (String) newRule.get("temperature"),
						false);
				RuleTerm term2 = new RuleTerm(rblock.getVariable("ambient"), (String) newRule.get("ambient"), false);
				RuleExpression antecedent;
				if (newRule.get("Connector").equals("OR")) {
					antecedent = new RuleExpression(term1, term2, RuleConnectionMethodOrMax.get());
				} else {
					antecedent = new RuleExpression(term1, term2, RuleConnectionMethodAndMin.get());
				}
				rule.setAntecedents(antecedent);
				rule.addConsequent(rblock.getVariable("blind"), (String) newRule.get("blind"), false);
				System.out.println("New rule added:");
				System.out.println(rule);
				rblock.add(rule);
				System.out.println("Updated rules block is ");
				for (Rule rules : rblock.getRules()) {
					System.out.println(rules);
				}
				processFCLFile();
				return new JSONRPC2Response("Success", req.getID());
			} else if (req.getMethod().equals("deleteRule")) {
				Map<String, Object> deleteRule = req.getNamedParams();
				int index = Integer.parseInt((String) deleteRule.get("index"));
				System.out.println("Deleting rule at index " + index);
				rblock.remove(rblock.getRules().get(index));
				System.out.println("Updated rules block is ");
				for (Rule rules : rblock.getRules()) {
					System.out.println(rules);
				}
				processFCLFile();
				return new JSONRPC2Response("Success", req.getID());
			} else if (req.getMethod().equals("editRule")) {
				Map<String, Object> editRule = req.getNamedParams();
				int index = Integer.parseInt((String) editRule.get("index"));
				System.out.println("Editing rule at index " + index);
				rblock.remove(rblock.getRules().get(index));
				Rule rule = new Rule("" + (rblock.getRules().size() + 1), rblock);
				RuleTerm term1 = null, term2 = null;
				if (editRule.get("temperature") != null) {
					term1 = new RuleTerm(rblock.getVariable("temperature"), (String) editRule.get("temperature"),
							false);
				}
				if (editRule.get("ambient") != null) {
					term2 = new RuleTerm(rblock.getVariable("ambient"), (String) editRule.get("ambient"), false);
				}
				RuleExpression antecedent;
				if (editRule.get("connector").equals("OR")) {
					antecedent = new RuleExpression(term1, term2, RuleConnectionMethodOrMax.get());
				} else {
					antecedent = new RuleExpression(term1, term2, RuleConnectionMethodAndMin.get());
				}
				rule.setAntecedents(antecedent);
				rule.addConsequent(rblock.getVariable("blind"), (String) editRule.get("blind"), false);
				rblock.add(rule);
				System.out.println("Updated rules block is ");
				for (Rule rules : rblock.getRules()) {
					System.out.println(rules);
				}
				processFCLFile();
				return new JSONRPC2Response("Success", req.getID());
			} else {
				// Method name not supported
				return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
			}
		}
	}

	/*
	 * This method basically writes the rule changes to FCL file.
	 */
	public static void processFCLFile() {
		try (PrintWriter pw = new PrintWriter("FuzzyLogic/smartBlind.fcl")) {
			pw.println(PiClient.fis.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Implements a handler for an "blindStatus" JSON-RPC method
	public static class BlindStatusHandler implements RequestHandler {

		// Reports the method names of the handled requests
		public String[] handledRequests() {

			return new String[] { "getBlindStatus" };
		}

		// Processes the requests
		public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {

			if (req.getMethod().equals("getBlindStatus")) {
				int adc_temperature = GpioReader.getTemp();
				int adc_ambient = GpioReader.getAmbient();
				float[] temperatureValues = PiClient.getTemperature(adc_temperature);
				String temperatureStatus = PiClient.getTemperatureStatus((int) temperatureValues[0]);
				String blindStatus = PiClient.getBlindPositionStatus((int) temperatureValues[0],
						PiClient.getAmbient(adc_ambient));
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				Map<String, String> blindStatusMap = new HashMap<String, String>();
				blindStatusMap.put("BlindStatus", blindStatus);
				blindStatusMap.put("TemperatureStatus", temperatureStatus);
				blindStatusMap.put("TemperatureCelsius", String.valueOf(temperatureValues[1]));
				blindStatusMap.put("TemperatureFarenheit", String.valueOf(temperatureValues[2]));
				blindStatusMap.put("AmbientStatus", PiClient.getAmbientStatus(PiClient.getAmbient(adc_ambient)));
				blindStatusMap.put("time", sdf.format(cal.getTime()));
				return new JSONRPC2Response(blindStatusMap, req.getID());
			} else {
				// Method name not supported
				return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
			}
		}
	}
}
