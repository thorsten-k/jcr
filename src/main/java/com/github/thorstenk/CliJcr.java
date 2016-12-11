package com.github.thorstenk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

import javax.jcr.Binary;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class CliJcr
{
	final static Logger logger = LoggerFactory.getLogger(CliJcr.class);
	
	private Repository repo;
	private DocumentNodeStore dns;
	
	private String user = "admin";
	private String pwd = "admin";
	
	public CliJcr()
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
		
		String fileName = "test.bin";
		
		Session session = repo.login(new SimpleCredentials(user,pwd.toCharArray()));
		Node root = session.getRootNode();
		

		if(root.hasNode(fileName))
		{
			logger.debug("Remove file node for testing ...");
			root.getNode(fileName).remove();
			session.save();
		}
		
		Node file = root.addNode(fileName,"nt:file");
	
		Node content = file.addNode("jcr:content","nt:resource");
		
		content.setProperty("jcr:encoding", "myProperty1");
		
		// Here I want to use my own property mame "x"
		content.setProperty("x", "myProperty2");
			
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