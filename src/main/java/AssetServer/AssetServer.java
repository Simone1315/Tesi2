package AssetServer;

import GlobalClasses.*;
import Utilities.ConstValues;
import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssetServer {
    private static final Logger  logger = LogManager.getLogger(AssetServer.class);
    private static TDBDataset assetOntholgyTDB;
    private static TDBDataset createdAssetTDB;
    private static TDBDataset createdPoliciesTDB;


    public static void main(String[] args) throws Exception {

        org.apache.log4j.BasicConfigurator.configure(new NullAppender());

        System.out.println("The asset manager server is running.");
        int clientNumber = 0;

        String directory = "C:\\Users\\simone\\IdeaProjects\\Tesi2\\AssetTDB" ;
        assetOntholgyTDB = new TDBDataset(directory);

        String secondDirectory = "C:\\Users\\simone\\IdeaProjects\\Tesi2\\CreatedAssetTDB" ;
        createdAssetTDB  = new TDBDataset( secondDirectory );

        String thirdDirectory = "C:\\Users\\simone\\IdeaProjects\\Tesi2\\CreatedPoliciesTDB" ;
        createdPoliciesTDB  = new TDBDataset( thirdDirectory );

        createdPoliciesTDB.addStatementToTDB("createdPoliciesModel", "policy1",ConstValues.description,"Noleggio 7 giorni - 2,99€");
        createdPoliciesTDB.addStatementToTDB("createdPoliciesModel", "policy2",ConstValues.description,"Acquisto definitivo - 9,99€");
        createdPoliciesTDB.addStatementToTDB("createdPoliciesModel", "policy1",ConstValues.hasHumanReadableName,"Long Description");
        createdPoliciesTDB.addStatementToTDB("createdPoliciesModel", "policy2",ConstValues.hasHumanReadableName,"Long Description");


        TDBLoader assetLoader = new TDBLoader(assetOntholgyTDB.getDataset(), ConstValues.assetNameModel);
        assetLoader.loadData("C:\\Users\\simone\\IdeaProjects\\Tesi2\\src\\main\\java\\GlobalClasses\\assetOnthology.ttl");

        ServerSocket listener = new ServerSocket(9898);
        try {
            while (true) {
                new OnthologyReader(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class OnthologyReader extends Thread {
        ObjectOutputStream out = null;
        private Socket socket;
        private int clientNumber;
        private ObjectInputStream in;
        List<String> basicTypes = ConstValues.basicTypes;


        public OnthologyReader(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("New connection with client# " + clientNumber + " at " + socket);
        }

        //This thread reads the input string from the client and sends back
        //all subclasses and all attributes
        public void run() {
            try {

                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream((socket.getInputStream()));


                // Send a welcome message to the client.
                out.writeObject("Hello, you are client #" + clientNumber + ".");


                // Get messages from the client, line by line; return attributes and subclasses
                while (true) {
                    String input = (String)in.readObject();
                    System.out.println(input);
                    if(input.equals("InterfaceCreation")) {
                        writeInterafaces();
                    }
                    else if(input.equals("WriteResults"))
                    {
                        writeResultToTDB();
                    }
                    else if(input.equals("GetAllAssets"))
                    {
                        SearchableList sL = new SearchableList(null, createdAssetTDB,assetOntholgyTDB,createdPoliciesTDB, "createdAssetModel", ConstValues.assetNameModel,ConstValues.assetManager);
                        System.out.println(sL.getAllNames());
                        out.writeObject(sL);
                    }
                    else if (input.equals("GetPolicies"))
                    {
                        System.out.println("666");
                        sendPolicies();
                    }
                }
            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client# " + clientNumber + " closed");
            }
        }


        //This command is used to create new interfaces
        public void writeInterafaces() throws IOException, ClassNotFoundException {
            while(true) {
                String input = (String)in.readObject();
                System.out.println(input);
                if(input.equals("."))
                    break;
                List<Attribute> attributes = writeAttributes(input,false, null);
                sendAttributes(attributes);
                writeSubclasses(input);
            }
        }

        private void writeResultToTDB()
        {

            try {
                String lastURIName = (String)in.readObject();
                String lastURI = (String)in.readObject();
                String newID = lastURIName + UUID.randomUUID().toString();
                List<FormResults> results = (List<FormResults>)in.readObject();
                createdAssetTDB.addStatementToTDB("createdAssetModel",newID,ConstValues.rdfType,lastURI);
                for(FormResults f : results)
                {
                    f.printResult();
                    List<Result> res = f.getResults();
                    for(Result r : res) {
                        if(r.getDifferentSubject() != null)
                        {
                            for(String value: r.getValues())
                                createdAssetTDB.addStatementToTDB( "createdAssetModel", r.getDifferentSubject(), r.getURI(),value);
                        }
                        else {
                            for (String value : r.getValues())
                                createdAssetTDB.addStatementToTDB("createdAssetModel", newID, r.getURI(), value);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        private void writeSubclasses(String input) {
            List<Subclass> strings = new ArrayList<>();
            List<Statement> result = assetOntholgyTDB.getStatements(ConstValues.assetNameModel, null, ConstValues.subclassOf, input );

            for(Statement s : result)
            {
                List<Statement> stat = assetOntholgyTDB.getStatements(ConstValues.assetNameModel, s.getSubject().toString(), ConstValues.hasHumanReadableName, null);
                String readableName = (stat).get(0).getObject().toString();
                strings.add( new Subclass(s.getSubject().toString(),readableName));
            }
            try {
                for(Subclass a : strings)
                {
                    out.writeObject(a);
                }
                out.writeObject(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private List<Attribute> writeAttributes(String input, boolean nested,String parent) {

            List<Statement> result = assetOntholgyTDB.getStatements( ConstValues.assetNameModel, null, ConstValues.isTotal, input );
            System.out.println( ConstValues.assetNameModel+ " size: " + result.size() + "\n\t" + result);
            List<Attribute> attributes = new ArrayList<>();


            for(Statement s : result)
            {
                boolean mandatory = false;
                boolean isFunctional = false;
                boolean alreadyDefined = false;
                List<Statement> stat = assetOntholgyTDB.getStatements(ConstValues.assetNameModel, s.getSubject().toString(), ConstValues.hasHumanReadableName, null);
                List<Statement> rangeStat = assetOntholgyTDB.getStatements(ConstValues.assetNameModel, s.getSubject().toString(), ConstValues.range, null);

                String readableName = (stat).get(0).getObject().toString();
                String range = rangeStat.get(0).getObject().toString();
                System.out.println(readableName);
                if(assetOntholgyTDB.getStatements(ConstValues.assetNameModel, s.getSubject().toString(),ConstValues.mandatoryFor, ConstValues.assetManager ).size() > 0)
                {
                    mandatory = true;
                }

                if(assetOntholgyTDB.getStatements(ConstValues.assetNameModel, s.getSubject().toString(),ConstValues.rdfType,ConstValues.functionalProperty ).size() > 0)
                {
                    isFunctional= true;
                }

                if(assetOntholgyTDB.getStatements(ConstValues.assetNameModel, s.getSubject().toString(),ConstValues.rdfType, ConstValues.alreadyDefined).size() > 0)
                {
                    alreadyDefined = true;
                }

                boolean hasNesting = false;

                if(!basicTypes.contains(range))
                {
                    hasNesting = true;
                }
                String myParent = null;

                if(nested)
                    myParent = parent;



                attributes.add(new Attribute(s.getSubject().toString(),readableName, mandatory,!isFunctional,range,myParent,hasNesting,alreadyDefined));


                if(!basicTypes.contains(range))
                {
                    attributes.addAll(writeAttributes(range,true,s.getSubject().toString()));
                }


            }
            return attributes;

        }

        private void sendAttributes(List<Attribute> attributes)
        {
            try {
                for(Attribute a : attributes)
                {
                    out.writeObject(a);
                }
                out.writeObject(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendPolicies()
        {
            List<Policy> policies = new ArrayList<>();
            List<Statement> statements = createdPoliciesTDB.getStatements("createdPoliciesModel",null, ConstValues.description, null);
            for(Statement s : statements)
            {
                policies.add(new Policy(s.getSubject().toString(),s.getObject().toString()));
            }
            try {
                out.writeObject(policies);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void log(String message) {
            System.out.println(message);
        }




}}



