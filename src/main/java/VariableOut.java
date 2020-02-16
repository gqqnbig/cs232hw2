import syntaxtree.Identifier;

public class VariableOut extends FlowSensitiveNullPointerAnalysisVariable
{

	public VariableOut(NullableIdentifierDefinition input, Location statement)
	{
		super(input, statement);
	}


	@Override
	public String getFunctionName()
	{
		return "out";
	}

}
