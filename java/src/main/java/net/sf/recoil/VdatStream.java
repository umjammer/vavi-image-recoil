// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class VdatStream extends TnyStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b < 128) {
			if (b == 0 || b == 1) {
				this.repeatCount = readValue();
				if (this.repeatCount < 0)
					return false;
			}
			else
				this.repeatCount = b;
			this.repeatValue = b == 0 ? -1 : readValue();
		}
		else {
			this.repeatCount = 256 - b;
			this.repeatValue = -1;
		}
		return true;
	}
}
