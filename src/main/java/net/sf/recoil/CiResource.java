// Generated automatically with "cito". Do not edit.
package net.sf.recoil;
import java.io.DataInputStream;
import java.io.IOException;

class CiResource
{
	static byte[] getByteArray(String name, int length)
	{
		DataInputStream dis = new DataInputStream(CiResource.class.getResourceAsStream(name));
		byte[] result = new byte[length];
		try {
			try {
				dis.readFully(result);
			}
			finally {
				dis.close();
			}
		}
		catch (IOException e) {
			throw new RuntimeException();
		}
		return result;
	}
}
