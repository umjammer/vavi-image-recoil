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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ZipRECOIL extends RECOIL
{
	private final Context context;
	private final Uri uri;

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
		catch (IOException e) {
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
	private Menu menu;

	private static boolean isZip(String filename)
	{
		int n = filename.length();
		return n >= 4 && filename.regionMatches(true, n - 4, ".zip", 0, 4);
	}

	private void showFilename(String filename)
	{
		setTitle(getString(R.string.viewing_title, filename));
	}

	private View getError(int message, String filename)
	{
		TextView textView = (TextView) getLayoutInflater().inflate(R.layout.error, null);
		textView.setText(getString(message, filename));
		return textView;
	}

	private RECOIL decode(String filename) throws IOException
	{
		InputStream is = null;
		try {
			is = getContentResolver().openInputStream(this.uri);
			RECOIL recoil;
			long fileLength = this.fileLength;
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
			return recoil.decode(filename, content, contentLength) ? recoil : null;
		}
		finally {
			if (is != null)
				is.close();
		}
	}

	private RECOIL decodeOrNull(String filename)
	{
		try {
			return decode(filename);
		}
		catch (IOException e) {
			return null;
		}
	}

	private Bitmap getBitmap(RECOIL recoil)
	{
		int[] pixels = recoil.getPixels();
		int width = recoil.getWidth();
		int height = recoil.getHeight();

		// Set alpha
		int pixelsLength = width * height;
		for (int i = 0; i < pixelsLength; i++)
			pixels[i] |= 0xff000000;

		Bitmap bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
		bitmap.setHasAlpha(false);
		return bitmap;
	}

	private View getGalleryView(int position, View convertView)
	{
		String filename = this.filenames.get(position);
		RECOIL recoil;
		try {
			recoil = decode(filename);
		}
		catch (IOException e) {
			return getError(R.string.error_reading_file, filename);
		}
		if (recoil == null)
			return getError(R.string.error_decoding_file, filename);

		ImageView imageView = convertView instanceof ImageView ? (ImageView) convertView : new ImageView(this);
		imageView.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.MATCH_PARENT, Gallery.LayoutParams.MATCH_PARENT));
		imageView.setImageBitmap(getBitmap(recoil));
		return imageView;
	}

	private View getZipView(String filename)
	{
		this.fileLength = ZIP_FILE_LENGTH;
		try (ZipInputStream zis = ZipRECOIL.open(this, this.uri)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					String name = entry.getName();
					if (RECOIL.isOurFile(name))
						this.filenames.add(name);
				}
			}
		}
		catch (IOException e) {
			return getError(R.string.error_reading_file, filename);
		}
		if (this.filenames.isEmpty())
			return getError(R.string.error_no_our_files, filename);

		this.gallery = (Gallery) getLayoutInflater().inflate(R.layout.gallery, null);
		this.gallery.setHorizontalFadingEdgeEnabled(false);
		this.gallery.setOnItemSelectedListener(this);
		this.gallery.setAdapter(new BaseAdapter() {
			public View getView(int position, View convertView, ViewGroup parent) { return getGalleryView(position, convertView); }
			public long getItemId(int position) { return position; }
			public Object getItem(int position) { return position; }
			public int getCount() { return Viewer.this.filenames.size(); }
		});
		return this.gallery;
	}

	private View getView(Uri uri)
	{
		Cursor cursor = getContentResolver().query(uri, new String[] { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE }, null, null, null);
		if (cursor == null)
			return getError(R.string.error_reading_file, uri.toString());
		String filename;
		long fileLength;
		try {
			if (!cursor.moveToNext())
				return getError(R.string.error_reading_file, uri.toString());
			filename = cursor.getString(0);
			fileLength = cursor.getLong(1);
		}
		finally {
			cursor.close();
		}

		this.uri = uri;
		this.filenames.clear();
		showFilename(filename);
		if (isZip(filename))
			return getZipView(filename);
		if (!RECOIL.isOurFile(filename))
			return getError(R.string.error_not_our_file, filename);
		this.filenames.add(filename);
		this.fileLength = fileLength;
		return getGalleryView(0, null);
	}

	private void open(Uri uri)
	{
		View view = getView(uri);
		setContentView(view);
		boolean enableMenu = !(view instanceof TextView);
		this.menu.findItem(R.id.menu_info).setVisible(enableMenu);
		this.menu.findItem(R.id.menu_share_as_png).setVisible(enableMenu);
		this.menu.findItem(R.id.menu_save_as_png).setVisible(enableMenu);
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
	{
		showFilename(this.filenames.get(position));
	}

	public void onNothingSelected(AdapterView<?> parent)
	{
		// can we ever get here?
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Uri uri = getIntent().getData();
		if (uri != null)
			open(uri);
	}

	private static final int OPEN_REQUEST_CODE = 1;
	private static final int SAVE_REQUEST_CODE = 2;

	private void pickOpenFile()
	{
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		startActivityForResult(intent, OPEN_REQUEST_CODE);
	}

	private String getFilename()
	{
		return this.filenames.get(this.filenames.size() == 1 ? 0 : this.gallery.getSelectedItemPosition());
	}

	private void showInfo()
	{
		if (this.filenames.isEmpty())
			return;
		RECOIL recoil = decodeOrNull(getFilename());
		if (recoil == null)
			return; // whole screen already contains the error message
		String message = getString(R.string.info_message, recoil.getPlatform(), recoil.getOriginalWidth(), recoil.getOriginalHeight(), recoil.getColors());
		new AlertDialog.Builder(this).setTitle(R.string.info_title).setMessage(message).show();
	}

	private boolean savePng(Uri uri, RECOIL recoil)
	{
		ContentResolver cr = getContentResolver();
		try (OutputStream os = cr.openOutputStream(uri)) {
			getBitmap(recoil).compress(Bitmap.CompressFormat.PNG, 0, os);
			return true;
		}
		catch (IOException e) {
			cr.delete(uri, null, null);
			return false;
		}
	}

	private void shareAsPng()
	{
		if (this.filenames.isEmpty())
			return;
		String filename = getFilename();
		RECOIL recoil = decodeOrNull(filename);
		if (recoil == null)
			return; // whole screen already contains the error message

		Long now = System.currentTimeMillis() / 1000;
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
		values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
		values.put(MediaStore.MediaColumns.DATE_ADDED, now);
		values.put(MediaStore.MediaColumns.DATE_MODIFIED, now);
		Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		if (!savePng(uri, recoil))
			return;

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/png");
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(intent);
	}

	private void pickSaveFile()
	{
		if (this.filenames.isEmpty())
			return;
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/png");
		intent.putExtra(Intent.EXTRA_TITLE, getFilename() + ".png");
		startActivityForResult(intent, SAVE_REQUEST_CODE);
	}

	private void save(Uri uri)
	{
		if (this.filenames.isEmpty())
			return;
		String filename = getFilename();
		RECOIL recoil = decodeOrNull(filename);
		if (recoil == null)
			return; // whole screen already contains the error message

		savePng(uri, recoil); // TODO: report error
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.viewer, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.menu_open:
			pickOpenFile();
			return true;
		case R.id.menu_info:
			showInfo();
			return true;
		case R.id.menu_share_as_png:
			shareAsPng();
			return true;
		case R.id.menu_save_as_png:
			pickSaveFile();
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK || data == null)
			return;
		if (requestCode == OPEN_REQUEST_CODE)
			open(data.getData());
		else if (requestCode == SAVE_REQUEST_CODE)
			save(data.getData());
	}
}
