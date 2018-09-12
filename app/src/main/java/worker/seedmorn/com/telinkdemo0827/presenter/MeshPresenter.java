package worker.seedmorn.com.telinkdemo0827.presenter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import worker.seedmorn.com.telinkdemo0827.IMeshView;
import worker.seedmorn.com.telinkdemo0827.R;
import worker.seedmorn.com.telinkdemo0827.app.TelinkMyApplication;
import worker.seedmorn.com.telinkdemo0827.model.Mesh;
import worker.seedmorn.com.telinkdemo0827.model.SharedPreferencesHelper;
import worker.seedmorn.com.telinkdemo0827.utils.FileSystem;

/*
*============================================
*Author:  zhangyn
*Time:   2018/8/27
*Version: 1.0
*Description:This is ${DATA}
*============================================
*/
public class MeshPresenter {
    public Activity mContext;
    private IMeshView iMeshView;
    private TelinkMyApplication myApplication;

    public MeshPresenter(Activity context, IMeshView meshView, TelinkMyApplication application) {
        this.mContext = context;
        this.iMeshView = meshView;
        this.myApplication = application;
    }

    public void showCreateMeshDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_mesh, null);
        final EditText etName = view.findViewById(R.id.et_mesh_name);
        final EditText etPwd = view.findViewById(R.id.et_mesh_pws);
        etName.setText("telink_mesh1");
        etPwd.setText("123");
        if (etPwd.getText().toString().length() > 16 || etName.getText().toString().length() > 16) {
            Toast.makeText(mContext, "invalid! input max length: 16", Toast.LENGTH_LONG).show();
            return;
        }
        if (etPwd.getText().toString().replace(" ", "").contains(".") || etName.getText().toString().replace(" ", "").contains(".")) {
            Toast.makeText(mContext, "invalid! input should not contains '.' ", Toast.LENGTH_LONG).show();
            return;
        }

        final DialogPlus dialogPlus = DialogPlus.newDialog(mContext)
                .setContentHolder(new ViewHolder(view))
                .setExpanded(false)
                .setPadding(60, 50, 60, 80)
                .setGravity(Gravity.CENTER)
                .create();

        view.findViewById(R.id.btn_confim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String meshName = etName.getText().toString();
                String pwd = etPwd.getText().toString();
                if (!TextUtils.isEmpty(meshName) && !TextUtils.isEmpty(pwd)) {
                    iMeshView.updateTip();
                    Mesh mesh = (Mesh) FileSystem.readAsObject(mContext, etPwd.getText().toString() + "." + etName.getText().toString());
                    if (mesh == null) {
                        mesh = new Mesh();
                        mesh.name = etName.getText().toString();
                        mesh.password = etPwd.getText().toString();
                    }
                    if (mesh.saveOrUpdate(mContext)) {
                        myApplication.setupMesh(mesh);
                        SharedPreferencesHelper.saveMeshName(mContext, mesh.name);
                        SharedPreferencesHelper.saveMeshPassword(mContext, mesh.password);
                        Toast.makeText(mContext, "Save Mesh Success", Toast.LENGTH_LONG).show();
                    }
                    dialogPlus.dismiss();
                } else {
                    Toast.makeText(mContext, " mesh name or password can not be empty", Toast.LENGTH_LONG).show();
                    dialogPlus.dismiss();
                }
            }
        });
        dialogPlus.show();
    }
}
