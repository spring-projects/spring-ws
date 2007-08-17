using System;
using System.IO;
using System.ServiceModel;

namespace Spring.Ws.Samples.Mtom.Client.Wcf {

    public class Client {
        public static void Main(string[] args) {
            if (args.Length == 0) {
                Console.Error.WriteLine("Usage: image.exe [filename]");
                return;
            }
            ImageRepositoryClient wcfClient = new ImageRepositoryClient();
            try {            
                Image image = new Image();
                FileInfo file = new FileInfo(args[0]);
                image.name = file.Name;
                byte[] buf = new byte[file.Length];
                using (FileStream stream = file.OpenRead())
                {
                    stream.Read(buf, 0, buf.Length);
                }
                image.image = buf;
                DateTime start = DateTime.Now;
                wcfClient.StoreImage(image);
                TimeSpan interval = DateTime.Now - start;
                Console.WriteLine("StoreImage took: {0,4:N0} ms.", interval.TotalMilliseconds);
                                
                wcfClient.Close();

            } catch (TimeoutException ex) {
                Console.Error.WriteLine("The service operation timed out. " + ex.Message);
                wcfClient.Abort();
            } catch (FaultException ex) {
                Console.Error.WriteLine("SOAP Fault Code    {0}", ex.Code);
                Console.Error.WriteLine("SOAP Fault Reason: {0}", ex.Reason);
                wcfClient.Abort();
            } catch (CommunicationException ex) {
                Console.WriteLine(ex.GetType());
                Console.Error.WriteLine("There was a communication problem. " + ex.Message);
                Console.Error.WriteLine(ex.StackTrace);
                wcfClient.Abort();
            } catch (Exception ex) {
                Console.Error.WriteLine("Unknown exception. " + ex.Message);
                Console.Error.WriteLine(ex.StackTrace);
                wcfClient.Abort();
            }
        }
    }
}
