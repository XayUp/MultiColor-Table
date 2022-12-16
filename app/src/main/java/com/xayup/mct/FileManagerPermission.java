package com.xayup.mct;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.security.Permission;
import java.util.jar.Manifest;

public class FileManagerPermission extends AppCompatActivity {
	final String[] per = new String[]{"android.permission.MANAGER_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
	
	public int STORAGE_PERMISSION = 1000;
	public int ANDROID_11_REQUEST_PERMISSION_AMF = 1001;
	final public int android11per = 1;

	public void getTotalStoragePermission(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (!Environment.isExternalStorageManager()) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
				Uri uri = Uri.fromParts("package", "com.xayup.mct", null);
				intent.setData(uri);
				System.out.println("\n\n" + intent);
				startActivityForResult(intent, ANDROID_11_REQUEST_PERMISSION_AMF);
			}
		} else {
			if ((checkCallingPermission(per[0 + android11per])
					& checkCallingPermission(per[1 + android11per])) != PackageManager.PERMISSION_GRANTED)
				requestPermissions(per, STORAGE_PERMISSION);
		}
	}
	/*
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
	
	}
	*/

	public boolean permissionGranted(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.isExternalStorageManager();
		} else {
			if ((context.checkCallingPermission(per[0 + android11per])
					| context.checkCallingPermission(per[1 + android11per])) != PackageManager.PERMISSION_GRANTED) {
				return false;
			} else {
				return true;
			}
		}
	}
}