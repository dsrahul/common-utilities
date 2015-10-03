package fw.org.company.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.swing.table.TableModel;
import javax.xml.transform.dom.DOMResult;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.pox.dom.DomPoxMessageFactory;
import org.springframework.ws.transport.http.CommonsHttpMessageSender;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fw.org.company.form.FWFrame;

@Component("tablefactory")
public class FWResponseTableModelFactory {

	private String XML = "<DocFWImport><Header CompanyName=\"jl\" LoginName=\"fw-admin\" Password=\"cs\"/><Request><DocMasterBOL><DocBOL AdviseType=\"0\" ApplyFWOnly=\"true\" BOLNumber=\"30300092\" \t\tStartDate=\"<DATE>T00:00:01\" EndDate=\"<DATE>T23:59:59\" \t\tMaxSlots=\"500\" MessagePurpose=\"1002\" ProcessCode=\"30\" ScheduleKey=\"Planning\"><UDF DeliveryRatio=\"1\" Profit=\"2500\"/><DeliveryLocation GeocodingPolicy=\"2\" Zip=\"$$$$\" LocationType=\"CUSTOMER\" Territory=\"*\"/><BOLLine ItemQty=\"1\"><UDF Measure1=\"$MES1$\" Measure2=\"$MES2$\" Measure3=\"$MES3$\" ProductKey=\"72190/1\" Requirements=\"$REQ$\"/></BOLLine></DocBOL></DocMasterBOL></Request></DocFWImport>";
	private WebServiceTemplate adviseCallWebServiceTemplate;
	private static final String XPATH_DEL_WINDOW = "//STADMessage/Request/DocBOL/DeliveryWindow";
	private static final String FW_URI = "http://jlp-fleetwise-ba/STAD/Listener/DocBOLAdviceConfirmListener.asp";
	private static Log log = LogFactory.getLog(FWResponseTableModelFactory.class);
	// private final String adviseCall = "<DocFWImport>   <Header CompanyName=\"jl\" LoginName=\"fw-admin\" Password=\"cs\"/>   <Request>      <DocMasterBOL>         <DocBOL AdviseType=\"0\" ApplyFWOnly=\"true\" BOLNumber=\"30300092\" \t\tStartDate=\"<DATE>T00:00:01\" EndDate=\"<DATE>T23:59:59\" \t\tMaxSlots=\"500\" MessagePurpose=\"1002\" ProcessCode=\"30\" ScheduleKey=\"Planning\">            <UDF DeliveryRatio=\"1\" Profit=\"2500\"/>            <DeliveryLocation GeocodingPolicy=\"2\" Zip=\"$$$$\" LocationType=\"CUSTOMER\" Territory=\"*\"/>            <BOLLine ItemQty=\"1\">               <UDF Measure1=\"$MES1$\" Measure2=\"$MES2$\" Measure3=\"$MES3$\" ProductKey=\"72190/1\" Requirements=\"$REQ$\"/>            </BOLLine>         </DocBOL>      </DocMasterBOL>   </Request></DocFWImport>";
	public FWResponseTableModelFactory() throws Exception {
		init();
	}

	@PostConstruct
	protected void init() {
		this.adviseCallWebServiceTemplate = new WebServiceTemplate(new DomPoxMessageFactory());
		CommonsHttpMessageSender httpSender = new CommonsHttpMessageSender(new HttpClient());
		httpSender.setConnectionTimeout(30000);
		httpSender.setReadTimeout(30000);
		this.adviseCallWebServiceTemplate.setMessageSender(httpSender);
	}

	public TableModel getResultSetTableModel(FWFrame.FWQueryDTO fwQueryDTO) {
		Set sOfPostcodes = fwQueryDTO.getPostcodes();
		Stack resultList = new Stack();
		BigDecimal acceptableProfitability = fwQueryDTO.getProfitability();

		for (Iterator iter = sOfPostcodes.iterator(); iter.hasNext();) {
			String postcode = (String) iter.next();
			postcode = StringUtils.replace(postcode, " ", "");
			boolean callfailed = false;
			List deliveryWindowList = ListUtils.EMPTY_LIST;
			int counter = 0;
			FWAdviseCallResponseDTO adviseCallResponseDTO = null;
			while (counter < 20) {
				//JJSDate startDate = JJSDateAndTimeUtil.addToDay(fwQueryDTO.getStartDate(), Integer.valueOf(counter));
				Date startDate = DateUtils.addDays(fwQueryDTO.getStartDate(), Integer.valueOf(counter));

				String replacedPostcode = StringUtils.replace(XML, "<DATE>", 
						DateUtils.getFragmentInDays(startDate, Calendar.YEAR) + 
						"-" + DateUtils.getFragmentInDays(startDate, Calendar.MONTH) + 
						"-" + DateUtils.getFragmentInDays(startDate, Calendar.DATE));
				replacedPostcode = StringUtils.replace(replacedPostcode, "$$$$", postcode);
				replacedPostcode = StringUtils.replace(replacedPostcode, "$REQ$", fwQueryDTO.getRequirement());
				replacedPostcode = StringUtils.replace(replacedPostcode, "$MES1$", fwQueryDTO.getMeasure1().toString());
				replacedPostcode = StringUtils.replace(replacedPostcode, "$MES2$", fwQueryDTO.getMeasure2().toString());
				replacedPostcode = StringUtils.replace(replacedPostcode, "$MES3$", fwQueryDTO.getMeasure3().toString());
				StringSource source = new StringSource(replacedPostcode);
				counter++;
				DOMResult responseResult = new DOMResult();
				try {
					this.adviseCallWebServiceTemplate.sendSourceAndReceiveToResult(FW_URI, source, responseResult);
				}
				catch (RuntimeException e) {
					callfailed = true;
					break;
				}

				XPathExpression deliveryWindowXPathExpression = XPathExpressionFactory.createXPathExpression(XPATH_DEL_WINDOW);
				deliveryWindowList = deliveryWindowXPathExpression.evaluateAsNodeList(responseResult.getNode());
				if (!deliveryWindowList.isEmpty()) {
					adviseCallResponseDTO = unmarshallDeliveryWindow(deliveryWindowList, acceptableProfitability);
					if (adviseCallResponseDTO != null)
						break;
					continue;
				}

			}

			if (!callfailed) {
				String resourceKey = null;
				String earliestDateTime = null;
				String latestDateTime = null;
				BigDecimal profitability = null;
				if (adviseCallResponseDTO != null) {
					resourceKey = adviseCallResponseDTO.getResourceKey();
					earliestDateTime = adviseCallResponseDTO.getEarliestDateTime();
					latestDateTime = adviseCallResponseDTO.getLatestDateTime();
					profitability = adviseCallResponseDTO.getProfitability();
				}
				List row = new ArrayList();
				row.add(postcode);
				row.add(earliestDateTime);
				row.add(latestDateTime);
				row.add(profitability);
				row.add(resourceKey);
				resultList.add(row);
				iter.remove();
			}

		}

		if (resultList.isEmpty()) {
			return new FWResponseTableModel(ListUtils.EMPTY_LIST, resultList, sOfPostcodes);
		}

		List header = Arrays.asList(new String[] { "POSTCODE", "# of SLOTS", "START DATE TIME", "LATEST DATE TIME", "PROFITABILITY", "RESOURCE KEY" });
		return new FWResponseTableModel(header, resultList, sOfPostcodes);
	}

	private ListOrderedSet getLOfSortedUniquePostcode(String aPostcodes) {
		String[] split = StringUtils.split(aPostcodes, ",");
		ListOrderedSet decorate = ListOrderedSet.decorate(new TreeSet());
		decorate.addAll((Collection) Arrays.asList(split));
		return decorate;
	}

	private FWAdviseCallResponseDTO unmarshallDeliveryWindow(List anDeliveryWindowList, BigDecimal anAcceptableProfitability) {
		List lOfAdviseCallResponseDTO = new ArrayList();
		for (int i = 0; i < anDeliveryWindowList.size();) {
			Node deliveryWindowNode = (Node) anDeliveryWindowList.get(i);
			Element deliveryWindowElement = (Element) deliveryWindowNode;

			BigDecimal anProfitability = null;
			if ((deliveryWindowElement.getAttribute("Profitability") != null) && (deliveryWindowElement.getAttribute("Profitability").length() != 0)) {
				anProfitability = new BigDecimal(deliveryWindowElement.getAttribute("Profitability"));
			}

			if ((anProfitability.compareTo(anAcceptableProfitability) < 0) || (deliveryWindowElement.getAttribute("SlotID") == null) || (deliveryWindowElement.getAttribute("SlotID").length() == 0))
				break;
			int anSlotId = 0;
			if (StringUtils.isNumeric(deliveryWindowElement.getAttribute("SlotID"))) {
				anSlotId = Integer.valueOf(deliveryWindowElement.getAttribute("SlotID")).intValue();
			}
			int anAdviseResponseId = 0;
			if ((deliveryWindowElement.getAttribute("AdviseResponseID") != null) && (deliveryWindowElement.getAttribute("AdviseResponseID").length() != 0) && (StringUtils.isNumeric(deliveryWindowElement.getAttribute("AdviseResponseID")))) {
				anAdviseResponseId = Integer.valueOf(deliveryWindowElement.getAttribute("AdviseResponseID")).intValue();
			}
			String anEarliestDateTime = deliveryWindowElement.getAttribute("EarliestDateTime");
			String anLatestDateTime = deliveryWindowElement.getAttribute("LatestDateTime");
			int anPreceedingStopId = 0;
			if ((deliveryWindowElement.getAttribute("PrecedingStopID") != null) && (deliveryWindowElement.getAttribute("PrecedingStopID").length() != 0) && (StringUtils.isNumeric(deliveryWindowElement.getAttribute("PrecedingStopID")))) {
				anPreceedingStopId = Integer.valueOf(deliveryWindowElement.getAttribute("PrecedingStopID")).intValue();
			}
			String anResourceKey = deliveryWindowElement.getAttribute("ResourceKey");

			int anRouteId = 0;
			if ((deliveryWindowElement.getAttribute("RouteID") != null) && (deliveryWindowElement.getAttribute("RouteID").length() != 0) && (StringUtils.isNumeric(deliveryWindowElement.getAttribute("RouteID")))) {
				anRouteId = Integer.valueOf(deliveryWindowElement.getAttribute("RouteID")).intValue();
			}
			int anScore = 0;
			if ((deliveryWindowElement.getAttribute("Score") != null) && (deliveryWindowElement.getAttribute("Score").length() != 0) && (StringUtils.isNumeric(deliveryWindowElement.getAttribute("Score")))) {
				anScore = Integer.valueOf(deliveryWindowElement.getAttribute("Score")).intValue();
			}
			int anStopNumber = 0;
			if ((deliveryWindowElement.getAttribute("StopNumber") != null) && (deliveryWindowElement.getAttribute("StopNumber").length() != 0) && (StringUtils.isNumeric(deliveryWindowElement.getAttribute("StopNumber")))) {
				anStopNumber = Integer.valueOf(deliveryWindowElement.getAttribute("StopNumber")).intValue();
			}
			int anType = 0;
			if ((deliveryWindowElement.getAttribute("Type") != null) && (deliveryWindowElement.getAttribute("Type").length() != 0) && (StringUtils.isNumeric(deliveryWindowElement.getAttribute("Type")))) {
				anType = Integer.valueOf(deliveryWindowElement.getAttribute("Type")).intValue();
			}
			int anWindowEvalParmsPrecedingStopId = 0;
			int anWindowEvalParmsRouteId = 0;
			int anWindowEvalParmsStopNumber = 0;
			if (deliveryWindowElement.getElementsByTagName("WindowEvalParms") != null) {
				Element deliveryWindowEvalParmsElement = (Element) deliveryWindowElement.getElementsByTagName("WindowEvalParms").item(0);
				if ((deliveryWindowElement.getAttribute("PrecedingStopID") != null) && (deliveryWindowElement.getAttribute("PrecedingStopID").length() != 0) && (StringUtils.isNumeric(deliveryWindowElement.getAttribute("PrecedingStopID")))) {
					anWindowEvalParmsPrecedingStopId = Integer.valueOf(deliveryWindowEvalParmsElement.getAttribute("PrecedingStopID")).intValue();
				}
				if ((deliveryWindowElement.getAttribute("RouteID") != null) && (deliveryWindowElement.getAttribute("RouteID").length() != 0) && (StringUtils.isNumeric(deliveryWindowElement.getAttribute("RouteID")))) {
					anWindowEvalParmsRouteId = Integer.valueOf(deliveryWindowEvalParmsElement.getAttribute("RouteID")).intValue();
				}
				if ((deliveryWindowElement.getAttribute("StopNumber") != null) && (deliveryWindowElement.getAttribute("StopNumber").length() != 0) && (StringUtils.isNumeric(deliveryWindowElement.getAttribute("StopNumber")))) {
					anWindowEvalParmsStopNumber = Integer.valueOf(deliveryWindowEvalParmsElement.getAttribute("StopNumber")).intValue();
				}
			}

			String territory = deliveryWindowElement.getAttribute("Territory");

			FWAdviseCallResponseDTO adviseCallResponseDTO = new FWAdviseCallResponseDTO(anAdviseResponseId, anSlotId, anEarliestDateTime, anLatestDateTime, anPreceedingStopId, anProfitability, anResourceKey, anRouteId, anScore, anStopNumber, anType,
					anWindowEvalParmsPrecedingStopId, anWindowEvalParmsRouteId, anWindowEvalParmsStopNumber, territory);
			lOfAdviseCallResponseDTO.add(adviseCallResponseDTO);
			return adviseCallResponseDTO;
		}
		return null;
	}

	class FWAdviseCallResponseDTO {

		private static final long serialVersionUID = 1L;
		private int adviseResponseId;
		private int deliveryWindowSlotId;
		private String earliestDateTime;
		private String latestDateTime;
		private int precedingStopId;
		private BigDecimal profitability;
		private String resourceKey;
		private int routeId;
		private int score;
		private int stopNumber;
		private int type;
		private int windowEvalParmsPrecedingStopId;
		private int windowEvalParmsRouteId;
		private int windowEvalParmsStopNumber;
		private String territory;

		public FWAdviseCallResponseDTO() {
		}

		public FWAdviseCallResponseDTO(int anAdviseResponseId,
				int anDeliveryWindowSlotId,
				String anEarliestDateTime,
				String anLatestDateTime,
				int anPrecedingStopId,
				BigDecimal anProfitability,
				String anResourceKey,
				int anRouteId,
				int anSscore,
				int anStopNumber,
				int anType,
				int anWindowEvalParmsPrecedingStopId,
				int anWindowEvalParmsRouteId,
				int anWindowEvalParmsStopNumber,
				String anTerritory) {
			this.adviseResponseId = anAdviseResponseId;
			this.deliveryWindowSlotId = anDeliveryWindowSlotId;
			this.earliestDateTime = anEarliestDateTime;
			this.latestDateTime = anLatestDateTime;
			this.precedingStopId = anPrecedingStopId;
			this.profitability = anProfitability;
			this.resourceKey = anResourceKey;
			this.routeId = anRouteId;
			this.score = anSscore;
			this.stopNumber = anStopNumber;
			this.type = anType;
			this.windowEvalParmsPrecedingStopId = anWindowEvalParmsPrecedingStopId;
			this.windowEvalParmsRouteId = anWindowEvalParmsRouteId;
			this.windowEvalParmsStopNumber = anWindowEvalParmsStopNumber;
			this.territory = anTerritory;
		}

		public int getAdviseResponseId() {
			return this.adviseResponseId;
		}

		public int getDeliveryWindowSlotId() {
			return this.deliveryWindowSlotId;
		}

		public String getEarliestDateTime() {
			return this.earliestDateTime;
		}

		public String getLatestDateTime() {
			return this.latestDateTime;
		}

		public int getPrecedingStopId() {
			return this.precedingStopId;
		}

		public BigDecimal getProfitability() {
			return this.profitability;
		}

		public String getResourceKey() {
			return this.resourceKey;
		}

		public int getRouteId() {
			return this.routeId;
		}

		public int getScore() {
			return this.score;
		}

		public int getStopNumber() {
			return this.stopNumber;
		}

		public int getType() {
			return this.type;
		}

		public int getWindowEvalParmsPrecedingStopId() {
			return this.windowEvalParmsPrecedingStopId;
		}

		public int getWindowEvalParmsRouteId() {
			return this.windowEvalParmsRouteId;
		}

		public int getWindowEvalParmsStopNumber() {
			return this.windowEvalParmsStopNumber;
		}

		public String getTerritory() {
			return this.territory;
		}
	}
}