package com.xayup.mct;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Binder;
import android.provider.Settings;
import android.net.Uri;
import android.content.Intent;
import android.os.Environment;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.ListAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.StringBuilder;

public class MainActivity extends AppCompatActivity {
	Context context;

	Button back;
	Button save_color_table;
	Button fab;

	final String FILE_EXTENSION = ".ct";

	ListView list_projects;

	final int PROJECTS = 0;
	final int COLOR_TABLE = 1;
	final int SELECTED_COLOR = Color.parseColor("#64858585");

	int current_activity = 0;

	File rootDir;

	List<Map<String, Integer>> list_color_table;
	List<String> files_list_index;

	TextView title;

	String traceLog = null;

	final String[] per = new String[] { "android.permission.MANAGER_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE" };

	public int STORAGE_PERMISSION = 1000;
	final public int ANDROID_11_REQUEST_PERMISSION_AMF = 1001;
	final public int android11per = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		if (logRastreador()) {
			setContentView(R.layout.crash);

			TextView textLog = findViewById(R.id.logText);
			textLog.setText(traceLog);

			Button copyToClipboard = findViewById(R.id.copyLog);
			Button finishApp = findViewById(R.id.exitcrash);
			Button restartApp = findViewById(R.id.restartApp);

			copyToClipboard.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					clipboard.setText(traceLog);
					Toast.makeText(getApplicationContext(), R.string.cop, Toast.LENGTH_SHORT).show();
				}
			});
			finishApp.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finishAffinity();
				}
			});
			restartApp.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					recreate();

				}
			});
		} else {
			Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
			startActivity();
		}
	}

	public boolean logRastreador() {
		if (this.getFileStreamPath("stack.trace").exists()) {
			traceLog = null;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(this.openFileInput("stack.trace")));
				String line = null;
				while ((line = reader.readLine()) != null) {
					traceLog += line + "\n";
				}

			} catch (FileNotFoundException fnfe) {
				// ...
			} catch (IOException ioe) {
				// ...
			}
			this.deleteFile("stack.trace");
			return true;
		}
		return false;
	}

	private void startActivity() {
		Boolean storage_granted = permissionGranted(context);
		if (storage_granted) {
			setContentView(R.layout.activity_main);
			activity();
		} else {
			//Crie um dialogo
			AlertDialog.Builder alert_get_storage_permission = new AlertDialog.Builder(this);
			alert_get_storage_permission.setTitle(getString(R.string.alert_dialog_app_storage_permission));
			alert_get_storage_permission.setMessage(getString(R.string.alert_dialog_app_storage_permission_msg));
			alert_get_storage_permission.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					//	new FileManagerPermission().getTotalStoragePermission(context);
					getTotalStoragePermission(context);
				}
			});
			alert_get_storage_permission.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					finishAffinity();
				}
			});
			AlertDialog show_diag_per = alert_get_storage_permission.create();
			show_diag_per.show();
		}
	}

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
			if (checkCallingPermission(per[0 + android11per]) != PackageManager.PERMISSION_GRANTED
					| checkCallingPermission(per[1 + android11per]) != PackageManager.PERMISSION_GRANTED)
				requestPermissions(per, STORAGE_PERMISSION);
		}
	}

	public boolean permissionGranted(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.isExternalStorageManager();
		} else {
			PackageManager pm = context.getPackageManager();
			return (PackageManager.PERMISSION_GRANTED == pm.checkPermission(per[0 + android11per],
					pm.getNameForUid(Binder.getCallingUid())));
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		switch (arg0) {
		case ANDROID_11_REQUEST_PERMISSION_AMF:
			startActivity();
			break;
		}
		super.onActivityResult(arg0, arg1, arg2);
	}

	@Override
	public void onRequestPermissionsResult(int arg0, String[] arg1, int[] arg2) {
		startActivity();
		super.onRequestPermissionsResult(arg0, arg1, arg2);
	}

	private void activity() {
		getSupportActionBar().hide();

		title = findViewById(R.id.action_bar_title);
		back = findViewById(R.id.title_bar_back);
		save_color_table = findViewById(R.id.title_bar_save);
		fab = findViewById(R.id.button_fab);

		//	title.setText(context.getString(R.string.list_projects_title));

		rootDir = new File(Environment.getExternalStorageDirectory() + "/MultiPad/LCT");
		if (!rootDir.exists()) {
			rootDir.mkdirs();
		}

		list_projects = findViewById(R.id.list_projects);
		files_list_index = new ArrayList<String>();
		fab.setOnClickListener(button_fab_list_projects_onclick());
		int index = 0;
		for (File f : rootDir.listFiles()) {
			final String file_name = f.getName().toString();
			if (file_name.substring(file_name.indexOf(".")).equals(FILE_EXTENSION) && f.isFile()) {
				files_list_index.add(index, file_name.replace(FILE_EXTENSION, ""));
				index++;
			}
		}
		list_color_table = new ArrayList<Map<String, Integer>>();
		list_projects.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files_list_index));
		back.setOnClickListener(button_back());

		list_projects.setOnItemLongClickListener(list_item_long_click());
		list_projects.setOnItemClickListener(list_projects_onclick());
	}

	public void rgb_seekbar_change(int seek_value, EditText edit_text, ImageView rgb_preview, int[] rgb) {
		edit_text.setText("" + seek_value);
		rgb_preview.setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
	}

	public AdapterView.OnItemClickListener list_projects_onclick() {

		return new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//	Toast.makeText(getApplicationContext(), rootDir.getPath().toString() + "/" + files_list_index.get(position), Toast.LENGTH_SHORT).show();
				current_activity = COLOR_TABLE;
				title.setText(context.getString(R.string.list_color_table));
				//	list_projects.setOnItemLongClickListener(list_item_long_click());
				try {
					BufferedReader ct_file = new BufferedReader(new FileReader(
							rootDir.getPath().toString() + "/" + files_list_index.get(position) + FILE_EXTENSION));
					String line = ct_file.readLine();
					list_color_table.clear();
					//Map<Integer, Map<String, Integer>> color_table = new HashMap<Integer, Map<String, Integer>>();
					int index = 0;
					Integer color_code = null;
					Integer r = null;
					Integer g = null;
					Integer b = null;
					while (line != null) {
						if (line.contains("{")) {
							color_code = null;
							r = null;
							g = null;
							b = null;
						} else if (line.contains("id=")) {
							if (color_code == null) {
								color_code = Integer.parseInt(line.replaceAll("id=", ""));
							}
						} else if (line.contains("r=")) {
							if (r == null) {
								r = Integer.parseInt(line.replaceAll("r=", ""));
							}
						} else if (line.contains("g=")) {
							if (g == null) {
								g = Integer.parseInt(line.replaceAll("g=", ""));
							}
						} else if (line.contains("b=")) {
							if (b == null) {
								b = Integer.parseInt(line.replaceAll("b=", ""));
							}
						} else if (line.contains("}")) {
							Map<String, Integer> rgb = new HashMap<String, Integer>();
							rgb.put("r", r);
							rgb.put("g", g);
							rgb.put("b", b);
							rgb.put("id", color_code);
							list_color_table.add(index, rgb);
							index++;
						}

						line = ct_file.readLine();
					}
					ct_file.close();
					//	Toast.makeText(getApplicationContext(), color_table.values() + "", Toast.LENGTH_SHORT).show();
				} catch (IOException f) {
				}
				list_projects.setAdapter(new CustomList(context, list_color_table));
				save_color_table.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						StringBuilder to_file = new StringBuilder();
						for (Map<String, Integer> table : list_color_table) {
							to_file.append("{");
							to_file.append("\nid=" + table.get("id"));
							to_file.append("\nr=" + table.get("r"));
							to_file.append("\ng=" + table.get("g"));
							to_file.append("\nb=" + table.get("b"));
							to_file.append("\n}\n");
						}
						try {
							FileWriter save_to = new FileWriter(rootDir.getPath().toString() + "/"
									+ files_list_index.get(position) + FILE_EXTENSION);
							save_to.write(to_file.toString());
							save_to.close();
							Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
						} catch (IOException e) {
							Toast.makeText(context, "Save failed :(", Toast.LENGTH_SHORT).show();
						}
					}
				});
				save_color_table.setVisibility(View.VISIBLE);
				back.setVisibility(View.VISIBLE);
				back.setOnClickListener(button_back());
				fab.setOnClickListener(button_fab_list_color_table_onclick());
				list_projects.setOnItemClickListener(list_color_table_onclick());
			}
		};
	}

	public AdapterView.OnItemClickListener list_color_table_onclick() {

		return new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos, long view_id) {
				title.setText(context.getString(R.string.list_projects_title));
				AlertDialog.Builder change_rgb = new AlertDialog.Builder(context);
				View change_rgb_layout = getLayoutInflater().inflate(R.layout.rgb_chage_layout, null);
				change_rgb.setView(change_rgb_layout);
				AlertDialog change_rgb_diag = change_rgb.create();
				//View items
				SeekBar seek_r = change_rgb_layout.findViewById(R.id.rgb_r_seek);
				SeekBar seek_g = change_rgb_layout.findViewById(R.id.rgb_g_seek);
				SeekBar seek_b = change_rgb_layout.findViewById(R.id.rgb_b_seek);
				EditText edit_r = change_rgb_layout.findViewById(R.id.rgb_r_edit);
				EditText edit_g = change_rgb_layout.findViewById(R.id.rgb_g_edit);
				EditText edit_b = change_rgb_layout.findViewById(R.id.rgb_b_edit);
				EditText edit_id = change_rgb_layout.findViewById(R.id.rgb_edit_id);
				EditText edit_hex = change_rgb_layout.findViewById(R.id.rgb_hex_edit);
				ImageView rgb_preview = change_rgb_layout.findViewById(R.id.rgb_color_preview);
				Button ok = change_rgb_layout.findViewById(R.id.rgb_change_ok);
				Button cancel = change_rgb_layout.findViewById(R.id.rgb_change_cancel);
				TextView change_title = change_rgb_layout.findViewById(R.id.rgb_change_title);
				
				//Variaveis
				int r = list_color_table.get(pos).get("r");
				int g = list_color_table.get(pos).get("g");
				int b = list_color_table.get(pos).get("b");
				int id = list_color_table.get(pos).get("id");
				
				edit_hex.setText("#FF"+Integer.toHexString(r).toUpperCase()+Integer.toHexString(g).toUpperCase()+Integer.toHexString(b).toUpperCase());
				//Definindo configs
				edit_hex.addTextChangedListener(new TextWatcher(){
					@Override
					public void afterTextChanged(Editable s) {}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
					}
					
					@Override
					public void onTextChanged(CharSequence s, int start,
					int before, int count) {
						String textValue = s.toString();
						if(textValue.length() == 9){
							final int r = Integer.decode("0x"+textValue.substring(3, 5)).intValue();
							final int g = Integer.decode("0x"+textValue.substring(5, 7)).intValue();
							final int b = Integer.decode("0x"+textValue.substring(7, 9)).intValue();
							edit_r.setText(""+r);
							edit_g.setText(""+g);
							edit_b.setText(""+b);
							seek_r.setProgress(r);
							seek_g.setProgress(g);
							seek_b.setProgress(b);
							rgb_preview.setBackgroundColor(Color.rgb(r, g, b));
						}
					}
				});
				edit_hex.setOnEditorActionListener(new TextView.OnEditorActionListener(){
					@Override
					public boolean onEditorAction(TextView text, int arg1, KeyEvent arg2) {
						
						
					    return true;
					}
				});
				
				change_title.setText(
						context.getString(R.string.rgb_change_title) + " #" + list_color_table.get(pos).get("id"));
				edit_r.setText("" + r);
				edit_g.setText("" + g);
				edit_b.setText("" + b);
				edit_id.setText("" + id);
				seek_r.setProgress(r);
				seek_g.setProgress(g);
				seek_b.setProgress(b);
				rgb_preview.setBackgroundColor(Color.rgb(r, g, b));

				//Definindo operações
				
				seek_r.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
						int[] rgb = { arg1, seek_g.getProgress(), seek_b.getProgress() };
						rgb_seekbar_change(arg1, edit_r, rgb_preview, rgb);
					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {
					}

					@Override
					public void onStopTrackingTouch(SeekBar arg0) {
					}

				});
				seek_g.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
						int[] rgb = { seek_r.getProgress(), arg1, seek_b.getProgress() };
						rgb_seekbar_change(arg1, edit_g, rgb_preview, rgb);
					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {
					}

					@Override
					public void onStopTrackingTouch(SeekBar arg0) {
					}

				});
				seek_b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
						int[] rgb = { seek_r.getProgress(), seek_g.getProgress(), arg1 };
						rgb_seekbar_change(arg1, edit_b, rgb_preview, rgb);
					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {
					}

					@Override
					public void onStopTrackingTouch(SeekBar arg0) {
					}
				});
				ok.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (("" + edit_id.getText()).replace(" ", "") == "") {
							Toast.makeText(context, context.getString(R.string.empty_field), Toast.LENGTH_SHORT).show();
						} else {
							int r = seek_r.getProgress();
							int g = seek_g.getProgress();
							int b = seek_b.getProgress();
							list_color_table.get(pos).put("r", r);
							list_color_table.get(pos).put("g", g);
							list_color_table.get(pos).put("b", b);
							list_color_table.get(pos).put("id", Integer.parseInt("" + edit_id.getText()));
							((ImageView) view.findViewById(R.id.color_view)).setBackgroundColor(Color.rgb(r, g, b));
							((TextView) view.findViewById(R.id.color_rgb))
									.setText("R= " + r + ", G= " + g + ", B= " + b);
							((TextView) view.findViewById(R.id.color_id))
									.setText("" + list_color_table.get(pos).get("id"));
							change_rgb_diag.dismiss();
						}
					}
				});
				cancel.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						change_rgb_diag.dismiss();
					}

				});
				change_rgb_diag.show();
			}
		};
	}

	public AdapterView.OnItemLongClickListener list_item_long_click() {
		return new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View view, int pos, long item_id) {
				title.setText(context.getString(R.string.selection_mode_title));
				back.setOnClickListener(button_cancel_selection_mode(current_activity));
				back.setVisibility(View.VISIBLE);
				back.setBackground(context.getDrawable(R.drawable.ic_clear_64));
				save_color_table.setVisibility(View.GONE);
				view.setTag(true);
				view.setBackgroundColor(SELECTED_COLOR);
				list_projects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View vieww, int arg2, long arg3) {
						if (vieww.getTag() != null && ((boolean) vieww.getTag()) == true) {
							vieww.setBackgroundColor(android.R.color.transparent);
							vieww.setTag(false);
						} else {
							vieww.setBackgroundColor(SELECTED_COLOR);
							vieww.setTag(true);
							//	vieww.setAlpha(0.5f);
						}
					}
				});
				fab.setBackground(context.getDrawable(R.drawable.ic_delete_64));
				fab.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						//	List<Map<String, Integer>> new_list = new ArrayList<Map<String, Integer>>();
						AlertDialog.Builder delete = new AlertDialog.Builder(context);
						delete.setTitle(context.getString(R.string.delete_diag_title));
						delete.setMessage(context.getString(R.string.delete_diag_desc));
						delete.setPositiveButton(context.getString(R.string.yes),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {

										int l = 0;
										for (int i = 0; i < list_projects.getChildCount(); i++) {
											if (list_projects.getChildAt(i).getTag() != null
													&& ((boolean) list_projects.getChildAt(i).getTag()) == true) {
												if (current_activity == COLOR_TABLE) {
													list_color_table.remove(l);
												} else if (current_activity == PROJECTS) {
													if (!(new File(rootDir.getAbsolutePath() + "/"
															+ files_list_index.get(l) + FILE_EXTENSION).delete())) {
														Toast.makeText(context,
																context.getString(R.string.delete_file_fail),
																Toast.LENGTH_SHORT).show();
													} else {
														files_list_index.remove(l);
													}
												}
											} else {
												l++;
											}
										}
										if (current_activity == PROJECTS) {
											list_projects.setAdapter(new ArrayAdapter<String>(context,
													android.R.layout.simple_list_item_1, files_list_index));
											fab.setOnClickListener(button_fab_list_projects_onclick());
											back.setVisibility(View.GONE);
											save_color_table.setVisibility(View.GONE);
											list_projects.setOnItemClickListener(list_projects_onclick());
											title.setText(context.getString(R.string.list_projects_title));
										} else if (current_activity == COLOR_TABLE) {
											list_projects.setAdapter(new CustomList(context, list_color_table));
											fab.setOnClickListener(button_fab_list_color_table_onclick());
											list_projects.setOnItemClickListener(list_color_table_onclick());
											save_color_table.setVisibility(View.VISIBLE);
											title.setText(context.getString(R.string.list_color_table));
										}
										fab.setBackground(context.getDrawable(R.drawable.ic_add_64));
										back.setBackground(context.getDrawable(R.drawable.ic_back_64));
									}
								});
						delete.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						});
						delete.create().show();
						//AlertDialog delete_diag = delete.create();
					}
				});
				return true;
			}
		};
	}

	public Button.OnClickListener button_back() {
		return new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (current_activity == COLOR_TABLE) {
					current_activity = PROJECTS;
					title.setText(context.getString(R.string.list_projects_title));
					list_projects.setAdapter(
							new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, files_list_index));
					list_projects.setOnItemClickListener(list_projects_onclick());
					back.setVisibility(View.GONE);
					save_color_table.setVisibility(View.GONE);
					fab.setOnClickListener(button_fab_list_projects_onclick());
				} else if (current_activity == PROJECTS) {

				}
			}
		};
	}

	public Button.OnClickListener button_cancel_selection_mode(int display) {
		return new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				back.setOnClickListener(button_back());
				back.setBackground(context.getDrawable(R.drawable.ic_back_64));
				fab.setBackground(context.getDrawable(R.drawable.ic_add_64));
				switch (display) {
				case COLOR_TABLE:
					list_projects.setAdapter(new CustomList(context, list_color_table));
					list_projects.setOnItemClickListener(list_color_table_onclick());
					fab.setOnClickListener(button_fab_list_color_table_onclick());
					save_color_table.setVisibility(View.VISIBLE);
					title.setText(context.getString(R.string.list_color_table));
					break;
				case PROJECTS:
					list_projects.setAdapter(
							new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, files_list_index));
					list_projects.setOnItemClickListener(list_projects_onclick());
					fab.setOnClickListener(button_fab_list_projects_onclick());
					title.setText(context.getString(R.string.list_projects_title));
					back.setVisibility(View.GONE);
					break;
				}
			}
		};
	}

	public Button.OnClickListener button_fab_list_projects_onclick() {
		return new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder new_file_props = new AlertDialog.Builder(context);
				View popup_layout = getLayoutInflater().inflate(R.layout.new_file_project_popup, null);
				new_file_props.setView(popup_layout);
				AlertDialog new_file_props_popup = new_file_props.create();
				new_file_props_popup.setCancelable(true);

				//Popup functions
				((Button) popup_layout.findViewById(R.id.nfp_popup_ok))
						.setOnClickListener(new Button.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								String name = ((EditText) popup_layout.findViewById(R.id.nfp_popup_file_name_input))
										.getText() + "";
								if (!name.replaceAll(" ", "").isEmpty()) {
									try {
										new File(rootDir.getAbsolutePath() + "/" + name + FILE_EXTENSION)
												.createNewFile();
										new_file_props_popup.dismiss();
										files_list_index.clear();
										int index = 0;
										for (File f : rootDir.listFiles()) {
											final String file_name = f.getName().toString();
											if (file_name.substring(file_name.indexOf(".")).equals(FILE_EXTENSION)
													&& f.isFile()) {
												files_list_index.add(index, file_name.replace(FILE_EXTENSION, ""));
												index++;
											}
										}
										list_projects.setAdapter(new ArrayAdapter<>(context,
												android.R.layout.simple_list_item_1, files_list_index));
										list_projects.setOnItemLongClickListener(list_item_long_click());
									} catch (IOException e) {
										Toast.makeText(context, "Create project error.", Toast.LENGTH_SHORT).show();
									}
								} else {
									Toast.makeText(context, context.getString(R.string.empty_field), Toast.LENGTH_SHORT)
											.show();
								}
							}
						});
				((Button) popup_layout.findViewById(R.id.nfp_popup_cancel))
						.setOnClickListener(new Button.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								new_file_props_popup.dismiss();
							}
						});
				new_file_props_popup.show();
			}
		};
	}

	public Button.OnClickListener button_fab_list_color_table_onclick() {
		return new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Map<String, Integer> new_color = new HashMap<String, Integer>();
				new_color.put("id", 0);
				new_color.put("r", 0);
				new_color.put("g", 0);
				new_color.put("b", 0);
				list_color_table.add(new_color);
				list_projects.setAdapter(new CustomList(context, list_color_table));
			}
		};
	}
}