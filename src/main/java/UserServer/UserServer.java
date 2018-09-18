package UserServer;
import AssetServer.TDBLoader;
import GlobalClasses.*;
import Utilities.ConstValues;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserServer {
    private static final Logger  logger = LogManager.getLogger(UserServer.class);
    private static TDBDataset userDatasetOnthology;
    private static TDBDataset createdUerDataset;
    private static TDBDataset assetOntholgyTDB ;
    private static TDBDataset createdAssetTDB;
    private static TDBDataset createdPoliciesTDB;

    public static void main(String[] args) throws Exception {

        org.apache.log4j.BasicConfigurator.configure(new NullAppender());

        System.out.println("The user manager server is running.");
        int clientNumber = 0;

        String directory = "C:\\Users\\simone\\IdeaProjects\\Tesi2\\UserTDB" ;
        userDatasetOnthology  = new TDBDataset( directory );

        String createdUserDirectory = "C:\\Users\\simone\\IdeaProjects\\Tesi2\\CreatedUserTDB" ;
        createdUerDataset  = new TDBDataset( createdUserDirectory );

        String assetOnthDirectory = "C:\\Users\\simone\\IdeaProjects\\Tesi2\\AssetTDB" ;
        assetOntholgyTDB = new TDBDataset(assetOnthDirectory);

        String createdAssetDirectory = "C:\\Users\\simone\\IdeaProjects\\Tesi2\\CreatedAssetTDB" ;
        createdAssetTDB  = new TDBDataset( createdAssetDirectory );

        String thirdDirectory = "C:\\Users\\simone\\IdeaProjects\\Tesi2\\CreatedPoliciesTDB" ;
        createdPoliciesTDB  = new TDBDataset( thirdDirectory );

        Model model = null;

        TDBLoader userLoader = new TDBLoader(userDatasetOnthology.getDataset(), ConstValues.userNameModel);
        userLoader.loadData("C:\\Users\\simone\\IdeaProjects\\Tesi2\\src\\main\\java\\GlobalClasses\\userOnthology.ttl");

        ServerSocket listener = new ServerSocket(9899);
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
        List<String> basicTypes = new ArrayList<>();


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
                in = new ObjectInputStream(socket.getInputStream());

                basicTypes.add(ConstValues.passwordRange);
                basicTypes.add(ConstValues.nonNegativeInteger);
                basicTypes.add(ConstValues.text);
                basicTypes.add(ConstValues.date);
                basicTypes.add(ConstValues.policy);


                // Send a welcome message to the client.
                out.writeObject("Hello, you are client #" + clientNumber + ".");

                    // Get messages from the client, line by line; return attributes and subclasses
                    while (true) {
                        Object input1 = in.readObject();
                        String input = (String)input1;
                        System.out.println(input);
                        if (input.equals("InterfaceCreation")) {
                            writeInterafaces();
                        }
                        else if(input.equals("Login"))
                        {
                            Login();
                        }
                        else if(input.equals("WriteResults"))
                        {
                            UserRegistration();
                        }
                        else if(input.equals("MyVcard"))
                        {
                            ShowVCard();
                        }
                        else if(input.equals("RebuildAsset"))
                        {
                            String subject = (String) in.readObject();
                            if(subject.equals("All"))
                                subject = null;
                            SearchableList sL = new SearchableList(subject,createdAssetTDB,assetOntholgyTDB,createdPoliciesTDB,"createdAssetModel", ConstValues.assetNameModel, ConstValues.navigationManager);
                            out.writeObject(sL);
                        }
                        else if (input.equals("GetSubclasses"))
                        {
                            String nextClass = (String)in.readObject();
                            out.writeObject(getLeafSubclasses(nextClass));
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

        private void writeSubclasses(String input) {
            List<Subclass> strings = new ArrayList<>();
            List<Statement> result = userDatasetOnthology.getStatements( ConstValues.userNameModel, null, ConstValues.subclassOf, input );

            for(Statement s : result)
            {
                List<Statement> stat = userDatasetOnthology.getStatements(ConstValues.userNameModel, s.getSubject().toString(), ConstValues.hasHumanReadableName, null);
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

        private List<Attribute> writeAttributes(String input, boolean nested, String parent) {

            List<Statement> result = userDatasetOnthology.getStatements( ConstValues.userNameModel, null, ConstValues.isTotal, input );
            System.out.println( ConstValues.userNameModel+ " size: " + result.size() + "\n\t" + result);
            List<Attribute> attributes = new ArrayList<>();


            for(Statement s : result)
            {
                boolean mandatory = false;
                boolean isFunctional = false;
                boolean alreadyDefined = false;
                List<Statement> stat = userDatasetOnthology.getStatements(ConstValues.userNameModel, s.getSubject().toString(), ConstValues.hasHumanReadableName, null);
                List<Statement> rangeStat = userDatasetOnthology.getStatements(ConstValues.userNameModel, s.getSubject().toString(), ConstValues.range, null);

                String readableName = (stat).get(0).getObject().toString();
                String range = rangeStat.get(0).getObject().toString();
                System.out.println(readableName);
                if(userDatasetOnthology.getStatements(ConstValues.userNameModel, s.getSubject().toString(),ConstValues.mandatoryFor, ConstValues.userManager ).size() > 0)
                {
                    mandatory = true;
                }

                if(userDatasetOnthology.getStatements(ConstValues.userNameModel, s.getSubject().toString(),ConstValues.rdfType,ConstValues.functionalProperty ).size() > 0)
                {
                    isFunctional= true;
                }

                if(userDatasetOnthology.getStatements(ConstValues.userNameModel, s.getSubject().toString(),ConstValues.rdfType, ConstValues.alreadyDefined).size() > 0)
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

                attributes.add(new Attribute(s.getSubject().toString(),readableName, mandatory,!isFunctional,range,parent,hasNesting,alreadyDefined));

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


        private void log(String message) {
            System.out.println(message);
        }


        public void writeInterafaces() throws IOException {
            while(true) {
                String input = null;
                try {
                    input = (String)in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if(input.equals("."))
                    break;
                List<Attribute> attributes = writeAttributes(input, false, null);
                sendAttributes(attributes);
                writeSubclasses(input);

            }
        }

        private void UserRegistration()
        {
            try {
                String lastURIName = (String)in.readObject();
                String lastURI = (String)in.readObject();
                String newID = lastURIName + UUID.randomUUID().toString();
                List<FormResults> results = (List<FormResults>)in.readObject();
                createdUerDataset.addStatementToTDB("createdUserModel",newID,ConstValues.rdfType,lastURI);
                for(FormResults f : results)
                {
                    f.printResult();
                    List<Result> res = f.getResults();
                    for(Result r : res) {
                        if(r.getURI().equals(ConstValues.hasPassword))
                        {
                            String hashedPW = BCrypt.hashpw(r.getValues().get(0),BCrypt.gensalt());
                            createdUerDataset.addStatementToTDB("createdUserModel",newID, r.getURI(), hashedPW);
                        }
                        else if(r.getDifferentSubject() != null)
                        {
                            for(String value: r.getValues())
                                createdUerDataset.addStatementToTDB("createdUserModel", r.getDifferentSubject(), r.getURI(),value);
                        }
                        else {
                            for (String value : r.getValues())
                                createdUerDataset.addStatementToTDB("createdUserModel", newID, r.getURI(), value);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void Login()
        {
            String username = "";
            String password = "";
            try {
                username = (String)in.readObject();
                password = (String)in.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            //Get the ID
            List<Statement> stats = createdUerDataset.getStatements("createdUserModel",null,ConstValues.vCardhasEmail,username);


            String ID = null;

            if(stats.size() > 0)
                ID = stats.get(0).getSubject().toString();
            else {
                try {
                    out.writeObject("Wrong");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            stats = createdUerDataset.getStatements("createdUserModel",ID,ConstValues.hasPassword,null);
            String DBPass = stats.get(0).getObject().toString();
            System.out.println(DBPass);


            if(BCrypt.checkpw(password,DBPass)) {
                try {
                    out.writeObject("OK");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    out.writeObject("Wrong");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        private List<Subclass> getLeafSubclasses(String classURI)
        {
            List<Subclass> subclasses = new ArrayList<>();
            List<Statement> statements = assetOntholgyTDB.getStatements(ConstValues.assetNameModel,null,ConstValues.subclassOf,classURI);
            for(Statement s: statements)
            {
                List<Statement> nameStatement = assetOntholgyTDB.getStatements(ConstValues.assetNameModel,s.getSubject().toString(),ConstValues.hasHumanReadableName,null);
                subclasses.add(new Subclass(s.getSubject().toString(),nameStatement.get(0).getObject().toString()));
            }

            return subclasses;
        }


        private void ShowVCard()
        {
            String username = "";
            try {
                username = (String)in.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            //Get the ID
            List<Statement> stats = createdUerDataset.getStatements("createdUserModel",null,ConstValues.vCardhasEmail,username);


            String ID = stats.get(0).getSubject().toString();

            SearchableList sL = new SearchableList(createdUerDataset,userDatasetOnthology,"createdUserModel",ConstValues.userNameModel, ID);
            try {
                out.writeObject(sL);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }}
