/*
 * Viewer.java - RECOIL for Android
 *
 * Copyright (C) 2013-2022  Piotr Fusik
 *
 * This file is part of RECOIL (Retro Computer Image Library),
 * see http://recoil.sourceforge.net
 *
 * RECOIL is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * RECOIL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RECOIL; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.sf.recoil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class RECOILException extends Exception
{
	RECOILException(String message)
	{
		super(message);
	}
}

class ZipRECOIL extends RECOIL
{
	private Context context;
	private Uri uri;

	ZipRECOIL(Context context, Uri uri)
	{
		this.context = context;
		this.uri = uri;
	}

	static ZipInputStream open(Context context, Uri uri) throws FileNotFoundException
	{
		return new ZipInputStream(context.getContentResolver().openInputStream(uri));
	}
	
	static long seekTo(ZipInputStream zis, String filename) throws IOException
	{
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			if (!entry.isDirectory() && filename.equals(entry.getName()))
				return entry.getSize();
		}
		throw new FileNotFoundException(filename);
	}

	@Override
	protected int readFile(String filename, byte[] content, int contentLength)
	{
		try (ZipInputStream zis = open(this.context, this.uri)) {
			seekTo(zis, filename);
			int got = 0;
			while (got < contentLength) {
				int i = zis.read(content, got, contentLength - got);
				if (i <= 0)
					break;
				got += i;
			}
			return got;
		}
		catch (IOException ex) {
			return -1;
		}
	}
}

public class Viewer extends Activity implements AdapterView.OnItemSelectedListener
{
	private Uri uri;
	private long fileLength;
	private static final long ZIP_FILE_LENGTH = -1;
	private final ArrayList<String> filenames = new ArrayList<String>();
	private Gallery gallery;
	private MenuItem infoMenuItem;

	private static boolean isZip(String filename)
	{
		int n = filename.length();
		return n >= 4 && filename.regionMatches(true, n - 4, ".zip", 0, 4);
	}

	private void open(Uri uri)
	{
		Cursor cursor = getContentResolver().query(uri, new String[] { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE }, null, null, null);
		try {
			if (cursor == null)
				throw new FileNotFoundException();
			String filename;
			long fileLength;
			try {
				if (!cursor.moveToNext())
					throw new FileNotFoundException();
				filename = cursor.getString(0);
				fileLength = cursor.getLong(1);
			}
			finally {
				cursor.close();
			}
			this.uri = uri;
			this.filenames.clear();
			if (isZip(filename)) {
				this.fileLength = ZIP_FILE_LENGTH;
				try (ZipInputStream zis = ZipRECOIL.open(this, uri)) {
					ZipEntry entry;
					while ((entry = zis.getNextEntry()) != null) {
						if (!entry.isDirectory()) {
							String name = entry.getName();
							if (RECOIL.isOurFile(name))
								this.filenames.add(name);
						}
					}
				}
			}
			else {
				this.filenames.add(filename);
				this.fileLength = fileLength;
			}
			this.gallery.setAdapter(new GalleryAdapter(this));
			this.infoMenuItem.setVisible(true);
		}
		catch (IOException ex) {
			// TODO
		}
	}

	int getFileCount()
	{
		return this.filenames.size();
	}

	RECOIL decode(int position) throws RECOILException
	{
		String filename = this.filenames.get(position);
		long fileLength = this.fileLength;
		RECOIL recoil;
		try {
			InputStream is = null;
			try {
				is = getContentResolver().openInputStream(this.uri);
				if (fileLength == ZIP_FILE_LENGTH) {
					ZipInputStream zis = new ZipInputStream(is);
					is = zis;
					fileLength = ZipRECOIL.seekTo(zis, filename);
					recoil = new ZipRECOIL(this, this.uri);
				}
				else
					recoil = new RECOIL();
				if (fileLength > Integer.MAX_VALUE)
					throw new IOException("File too long");
				int contentLength = (int) fileLength;
				byte[] content = new byte[contentLength];
				new DataInputStream(is).readFully(content);
				if (!recoil.decode(filename, content, contentLength))
					throw new RECOILException(getString(R.string.error_decoding_file, filename));
			}
			finally {
				if (is != null)
					is.close();
			}
		}
		catch (IOException ex) {
			throw new RECOILException(getString(R.string.error_reading_file, filename));
		}
		return recoil;
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
	{
		setTitle(getString(R.string.viewing_title, this.filenames.get(position)));
	}

	public void onNothingSelected(AdapterView<?> parent)
	{
		// can we ever get here?
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.gallery = (Gallery) getLayoutInflater().inflate(R.layout.gallery, null);
		this.gallery.setHorizontalFadingEdgeEnabled(false);
		this.gallery.setOnItemSelectedListener(this);
		setContentView(this.gallery);

		Uri uri = getIntent().getData();
		if (uri != null)
			open(uri);
	}

	private static final int OPEN_REQUEST_CODE = 1;

	private void pickFile()
	{
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		startActivityForResult(intent, OPEN_REQUEST_CODE);
	}

	private void showInfo()
	{
		if (this.filenames.isEmpty())
			return;
		RECOIL recoil;
		try {
			recoil = decode(this.gallery.getSelectedItemPosition());
		}
		catch (RECOILException ex) {
			// whole screen already contains the error message
			return;
		}
		String message = getString(R.string.info_message, recoil.getPlatform(), recoil.getOriginalWidth(), recoil.getOriginalHeight(), recoil.getColors());
		new AlertDialog.Builder(this).setTitle(R.string.info_title).setMessage(message).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.viewer, menu);
		this.infoMenuItem = menu.findItem(R.id.menu_info);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.menu_open:
			pickFile();
			return true;
		case R.id.menu_info:
			showInfo();
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == OPEN_REQUEST_CODE && resultCode == RESULT_OK && data != null)
			open(data.getData());
	}
}
