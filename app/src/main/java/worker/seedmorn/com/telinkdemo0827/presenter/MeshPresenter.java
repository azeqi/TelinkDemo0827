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
import worker.seedmorn.com.telinkdemo0827.model.SharedPreferencesHelper;

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

    public MeshPresenter(Activity context, IMeshView meshView) {
        this.mContext = context;
        this.iMeshView = meshView;
    }

    public void showCreateMeshDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_mesh, null);
        final EditText etName = view.findViewById(R.id.et_mesh_name);
        final EditText etPwd = view.findViewById(R.id.et_mesh_pws);
        etName.setText("zhang");
        etPwd.setText("123456");
        final DialogPlus dialogPlus = DialogPlus.newDialog(mContext)
                .setContentHolder(new ViewHolder(view))
                .setExpanded(false)
                .setPadding(60, 50, 60, 80)
                .setGravity(Gravity.CENTER)
                .create();

        view.findViewById(R.id.btn_confim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mesh = etName.getText().toString();
                String pwd = etPwd.getText().toString();
                if (!TextUtils.isEmpty(mesh) && !TextUtils.isEmpty(pwd)) {
                    SharedPreferencesHelper.saveMeshName(mContext, mesh);
                    SharedPreferencesHelper.saveMeshPassword(mContext, pwd);
                    iMeshView.updateTip();
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
