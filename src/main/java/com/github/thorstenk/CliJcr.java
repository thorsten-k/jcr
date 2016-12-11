
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import javax.jcr.Binary;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.NamingException;

import org.apache.commons.configuration.Configuration;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.blob.datastore.DataStoreBlobStore;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.spi.blob.BlobStore;
import org.jeesl.interfaces.controller.repository.JeeslJcrProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;



public class CliJcr
{
	final static Logger logger = LoggerFactory.getLogger(CliJcr.class);
	
	private DB db;
	private Repository repo;
	private DocumentNodeStore dns;
	
	private String user = "admin";
	private String pwd = "admin";
	
	public CliJcr() throws
	{

	}
	
	public void mongo()
	{
		DB db = new MongoClient("127.0.0.1", 27017).getDB("thor");
		dns = new DocumentMK.Builder().setMongoDB(db).getNodeStore();
		repo = new Jcr(new Oak(dns)).createRepository();
	}
	
	private void mixin() throws LoginException, RepositoryException
	{
		Random rnd = new Random();
		byte[] bytes = new byte[100];
		rnd.nextBytes(bytes);
		
		Session session = repo.login(new SimpleCredentials(user,pwd.toCharArray()));
		Node root = session.getRootNode();
		Node test;
		if (!root.hasNode("test")){test = root.addNode("test");session.save();}
		else{test = root.getNode("meis");}
		
		
		if(test.hasNode(xmlFile.getName()))
		{
			logger.debug("Remove file node for testing ...");
			test.getNode(xmlFile.getName()).remove();
			session.save();
		}
		
		Node file = test.addNode("name.txt","nt:file");
	
		Node content = file.addNode("jcr:content","nt:resource");
		if(xmlFile.isSetCategory())
		{
			
			content.setProperty(JeeslJcrProperty.jcrEncoding, xmlFile.getCategory());
			// We should not abuse this property, but use our own one ...
			content.setProperty("x", "y");
		}
		InputStream is = new ByteArrayInputStream(bytes);
		Binary binary = session.getValueFactory().createBinary(is);
		content.setProperty("jcr:data",binary);
		session.save();
	}
	
	public void close()
	{
		logger.warn("Shutting down");
		dns.dispose();
	}
	
    public static void main(String[] args) throws Exception
    {
    	CliJcr cli = new CliJcr();
    	cli.mongo();
    	cli.mixin();
    	cli.close();
    }
}