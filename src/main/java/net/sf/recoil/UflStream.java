// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class UflStream extends RleStream
{
	int escape;

	protected @Override boolean readCommand()
	{
		int b = readValue();
		if (b == this.escape) {
			this.repeatCount = readValue();
			if (this.repeatCount == 0)
				this.repeatCount = 256;
			b = readValue();
		}
		else
			this.repeatCount = 1;
		this.repeatValue = b;
		return b >= 0;
	}
}
