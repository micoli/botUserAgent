package net.sourceforge.peers.botUserAgent.misc;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.transport.SipRequest;

public class Utils {
	public static SipHeaderFieldName[] sipHeaderList;
	
	public static void init(){
		sipHeaderList = new SipHeaderFieldName[] {
			new SipHeaderFieldName(RFC3261.HDR_CALLID),
			new SipHeaderFieldName(RFC3261.HDR_CONTACT),
			new SipHeaderFieldName(RFC3261.HDR_CONTENT_ENCODING),
			new SipHeaderFieldName(RFC3261.HDR_CONTENT_LENGTH),
			new SipHeaderFieldName(RFC3261.HDR_CONTENT_TYPE),
			new SipHeaderFieldName(RFC3261.HDR_FROM),
			new SipHeaderFieldName(RFC3261.HDR_SUBJECT),
			new SipHeaderFieldName(RFC3261.HDR_SUPPORTED),
			new SipHeaderFieldName(RFC3261.HDR_TO),
			new SipHeaderFieldName(RFC3261.HDR_VIA)
		};
	}
	
	public static String getHeader(SipRequest sipRequest,String rfc3261) {
		SipHeaders sipHeaders = sipRequest.getSipHeaders();
		SipHeaderFieldName sipHeaderFieldName = new SipHeaderFieldName(rfc3261);
		SipHeaderFieldValue header = sipHeaders.get(sipHeaderFieldName);
		return header.getValue();
	}

	public static String getCallId(SipRequest sipRequest) {
		return getHeader(sipRequest,RFC3261.HDR_CALLID);
	}
	public static String getFrom(SipRequest sipRequest) {
		return getHeader(sipRequest,RFC3261.HDR_FROM);
	}
}
