using System;
using System.Web;
using System.Web.Services.Protocols;
using System.Xml;
using Microsoft.Web.Services2;
using Microsoft.Web.Services2.Security;
using Microsoft.Web.Services2.Security.Tokens;

namespace Spring.Ws.Samples.Airline.Client.CSharp {

	public class Client {
		public static void Main(string[] args) {
			try {
				AirlineService service = new AirlineService();
				if (args.Length > 0) {
					service.Destination = new Uri(args[0]);
				}
				SoapEnvelope request = new SoapEnvelope();
				request.SetBodyObject(@"<GetFrequentFlyerMileage xmlns=""http://www.springframework.org/spring-ws/samples/airline/schemas""/>");
				UsernameToken userToken = new UsernameToken("john","changeme", PasswordOption.SendHashed);
				request.Context.Security.Tokens.Add(userToken);
				SoapEnvelope response = service.GetFrequentFlyerMileage(request);
				int miles = XmlConvert.ToInt32(response.Body.FirstChild.InnerText);
				Console.Out.WriteLine("'john' has {0} frequent flyer miles", miles);
			} catch (SoapException ex) {
				Console.Error.WriteLine("SOAP Fault Code    {0}", ex.Code);
				Console.Error.WriteLine("SOAP Fault String: {0}", ex.Message);
			}
		}

	}
}
