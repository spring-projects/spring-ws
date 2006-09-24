using System;
using System.Web;
using System.Web.Services.Protocols;

namespace Spring.Ws.Samples.Airline.Client.CSharp {

	public class Client {
		public static void Main(string[] args) {
			try {
				AirlineService service = new AirlineService();
				if (args.Length > 0) {
					service.Url = args[0];
				} else {
					service.Url = "http://localhost:8080/airline/Airline";
				}
				// Get all flights on 31st Januari, 2006 from Amsterdam to Venice
				MessageGetFlightsRequest getFlightsRequest = new MessageGetFlightsRequest();
				getFlightsRequest.from = "AMS";
				getFlightsRequest.to = "VCE";
				getFlightsRequest.departureDate = new DateTime(2006,1,31);
				Console.WriteLine("Requesting flights on {0:d}", getFlightsRequest.departureDate);
				Flight[] flights = service.GetFlights(getFlightsRequest);
				Console.WriteLine("Got {0} results", flights.Length);
				if (flights.Length > 0) {
					// Book the first flight using John Doe as a frequent flyer
					MessageBookFlightRequest bookFlightRequest = new MessageBookFlightRequest();
					bookFlightRequest.flightNumber = flights[0].number;
					bookFlightRequest.departureTime = flights[0].departureTime;
					bookFlightRequest.passengers = new object[] { "john" };
					Ticket ticket = service.BookFlight(bookFlightRequest);
					WriteTicket(ticket);
				}
			} catch (SoapException ex) {
				Console.Error.WriteLine("SOAP Fault Code    {0}", ex.Code);
				Console.Error.WriteLine("SOAP Fault String: {0}", ex.Message);
			}
		}

		private static void WriteTicket(Ticket ticket) {
			Console.WriteLine("Ticket {0}", ticket.id);
			Console.WriteLine("Ticket issue date:\t{0:d}", ticket.issueDate);
			foreach (Name passenger in ticket.passengers) {
				WriteName(passenger);
			}
			WriteFlight(ticket.flight);
		}

		private static void WriteName(Name name) {
			Console.WriteLine("Passenger Name:");
			Console.WriteLine("{0} {1}", name.first, name.last);
			Console.WriteLine("------------");
		}

		private static void WriteFlight(Flight flight) {
			Console.WriteLine("{0:d}", flight.departureTime);
			Console.WriteLine("{0}\t{1}", flight.number, flight.serviceClass);
			Console.WriteLine("------------");
			Console.WriteLine("Depart:\t{0}-{1}\t{2:t}", flight.from.code, flight.from.name,
				flight.departureTime);
			Console.WriteLine("\t{0}", flight.from.city);
			Console.WriteLine("Arrive:\t{0}-{1}\t{2:t}", flight.to.code, flight.to.name,
				flight.arrivalTime);
			Console.WriteLine("\t{0}", flight.to.city);
		}
	}
}
