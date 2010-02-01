using System;
using System.IO;
using System.ServiceModel;

namespace Spring.Ws.Samples.Stock.Client.Wcf
{

    public class Client
    {
        public static void Main(string[] args)
        {
            StockClient wcfClient = new StockClient();
            try
            {
                string[] symbols = new string[] { "FABRIKAM", "CONTOSO" };
                StockQuoteRequest request = new StockQuoteRequest(symbols);

                Console.Out.WriteLine("Requesting quotes for {0}", symbols);
                StockQuote[] quotes = wcfClient.StockQuote(symbols);

                Console.Out.WriteLine("Got {0} results", quotes.Length);

                foreach (StockQuote quote in quotes)
                {
                    Console.Out.WriteLine();
                    Console.Out.WriteLine("Symbol: " + quote.Symbol);
                    Console.Out.WriteLine("\tName:\t\t\t" + quote.Name);
                    Console.Out.WriteLine("\tLast Price:\t\t" + quote.Last);
                    Console.Out.WriteLine("\tPrevious Change:\t" + quote.Change + "%");
                }

            }
            catch (TimeoutException ex)
            {
                Console.Error.WriteLine("The service operation timed out. " + ex.Message);
                wcfClient.Abort();
            }
            catch (FaultException ex)
            {
                Console.Error.WriteLine("SOAP Fault Code    {0}", ex.Code);
                Console.Error.WriteLine("SOAP Fault Reason: {0}", ex.Reason);
                wcfClient.Abort();
            }
            catch (CommunicationException ex)
            {
                Console.WriteLine(ex.GetType());
                Console.Error.WriteLine("There was a communication problem. " + ex.Message);
                Console.Error.WriteLine(ex.StackTrace);
                wcfClient.Abort();
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine("Unknown exception. " + ex.Message);
                Console.Error.WriteLine(ex.StackTrace);
                wcfClient.Abort();
            }
        }
    }
}
