package nullPointerAnalysis;

import utils.Location;

public class VariableIn extends FlowSensitiveNullPointerAnalysisVariable<NullableIdentifierDefinition>
{

	public VariableIn(NullableIdentifierDefinition input, Location statement)
	{
		super(input, statement);
	}


	@Override
	public String getFunctionName()
	{
		return "in";
	}
}