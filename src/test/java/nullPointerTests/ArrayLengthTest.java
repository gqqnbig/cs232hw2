import baseVisitors.AllocationVisitor;
import baseVisitors.ArrayLookupVisitor;
import baseVisitors.MessageSendCollector;
import org.junit.Assert;
import org.junit.Test;
import syntaxtree.ArrayLookup;
import syntaxtree.Goal;
import syntaxtree.MessageSend;
import typeAnalysis.ClassHierarchyAnalysis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;

public class ArrayLengthTest
{
	@Test
	public void test() throws FileNotFoundException, ParseException
	{
		FileInputStream stream = new FileInputStream("testcases/hw2/ArrayLengthTest.java");
		try {MiniJavaParser.ReInit(stream);} catch (Throwable e) {new MiniJavaParser(stream);}
		Goal goal = MiniJavaParser.Goal();

		ProgramStructureCollector.init(goal);
		ClassHierarchyAnalysis.init(goal);
		goal.accept(new AllocationVisitor());

		Solver.debugOut = new PrintStream(OutputStream.nullOutputStream());
		Assert.assertTrue("Null pointer exception should be thrown at line 6.", Solver.checkNullPointer(goal));
	}
}
