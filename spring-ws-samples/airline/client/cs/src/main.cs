using System;
using System.Web;
using System.Web.Services.Protocols;

namespace Spring.Ws.Samples.Airline {

	public class Client {
		public static void Main(string[] args) {
			AirlineService service = new AirlineService();		
			service.Url = "http://localhost:8080/airline/Airline";
			if (args.Length > 0) {
				service.Url = args[0];
			} else {
				service.Url = "http://localhost:8080/airline/Airline";
			}
			MessageGetFlightsRequest getFlightsRequest = new MessageGetFlightsRequest();
			getFlightsRequest.startOfPeriod = new DateTime(2006,1,31);
			getFlightsRequest.startOfPeriodSpecified = true;
			Console.WriteLine("Requesting flights after {0:d}", getFlightsRequest.startOfPeriod);
			Flight[] flights = service.GetFlights(getFlightsRequest);
			Console.WriteLine("Got {0} results", flights.Length);
			foreach (Flight flight in flights) {
				Console.WriteLine("Booking ticket for flight with number {0}", flight.number);
				MessageBookFlightRequest bookFlightRequest = new MessageBookFlightRequest();
				bookFlightRequest.flightNumber = flight.number;
				bookFlightRequest.customerId = 1L;
				Ticket ticket = service.BookFlight(bookFlightRequest);
				WriteTicket(ticket);
			}
		}
		
		private static void WriteTicket(Ticket ticket) {
			Console.WriteLine("Ticket");
			Console.WriteLine("Ticket issue date:\t{0:d}", ticket.issueDate);
			WriteCustomer(ticket.customer);
			WriteFlight(ticket.flight);
		}
		
		private static void WriteCustomer(Customer customer) {
			Console.WriteLine("Passenger Name:");
			Console.WriteLine("{0} {1}", customer.name.first, customer.name.last);
			Console.WriteLine("{0}", customer.id);
			Console.WriteLine("------------");			
		}
		
		private static void WriteFlight(Flight flight) {
			Console.WriteLine("{0:d}", flight.departureTime);
			Console.WriteLine("{0}\t{1}", flight.number, flight.serviceClass);
			Console.WriteLine("------------");
			Console.WriteLine("Depart:\t{0}-{1}\t{2:t}", flight.departureAirport.code, flight.departureAirport.name,
				flight.departureTime);
			Console.WriteLine("\t{0}", flight.departureAirport.city);
			Console.WriteLine("Arrive:\t{0}-{1}\t{2:t}", flight.arrivalAirport.code, flight.arrivalAirport.name,
				flight.arrivalTime);
			Console.WriteLine("\t{0}", flight.arrivalAirport.city);
		}
	}
}
