package com.avoscloud.chat.viewholder;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avoscloud.chat.R;
import com.avoscloud.chat.model.LCIMRedPacketMessage;
import com.avoscloud.chat.model.LeanchatUser;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.utils.RPRedPacketUtil;

import cn.leancloud.chatkit.viewholder.LCIMChatItemHolder;

/**
 * 点击红包消息，领取红包或者查看红包详情
 */
public class ChatItemRedPacketHolder extends LCIMChatItemHolder {

  private TextView mTvGreeting;

  private TextView mTvSponsorName;

  private TextView mTvPacketType;

  private LCIMRedPacketMessage mRedPacketMessage;

  public ChatItemRedPacketHolder(Context context, ViewGroup root, boolean isLeft) {
    super(context, root, isLeft);
  }

  @Override
  public void initView() {
    super.initView();
    if (isLeft) {
      conventLayout.addView(View.inflate(getContext(),
              R.layout.lc_chat_item_left_text_redpacket_layout, null));
    } else {
      conventLayout.addView(View.inflate(getContext(),
              R.layout.lc_chat_item_right_text_redpacket_layout, null)); /*红包view*/
    }
    RelativeLayout redPacketLayout = (RelativeLayout) itemView.findViewById(R.id.red_packet_layout);
    mTvGreeting = (TextView) itemView.findViewById(R.id.tv_money_greeting);
    mTvSponsorName = (TextView) itemView.findViewById(R.id.tv_sponsor_name);
    mTvPacketType = (TextView) itemView.findViewById(R.id.tv_packet_type);

    redPacketLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mRedPacketMessage) {
          openRedPacket(getContext(), mRedPacketMessage);
        }
      }
    });
  }

  @Override
  public void bindData(Object o) {
    super.bindData(o);
    AVIMMessage message = (AVIMMessage) o;
    if (message instanceof LCIMRedPacketMessage) {
      mRedPacketMessage = (LCIMRedPacketMessage) message;
      mTvGreeting.setText(mRedPacketMessage.getGreeting());
      mTvSponsorName.setText(mRedPacketMessage.getSponsorName());

      String redPacketType = mRedPacketMessage.getRedPacketType();
      if (!TextUtils.isEmpty(redPacketType) && redPacketType.equals(
              RPConstant.GROUP_RED_PACKET_TYPE_EXCLUSIVE)) {
        mTvPacketType.setVisibility(View.VISIBLE);
        mTvPacketType.setText(getContext().getResources().getString(
                R.string.exclusive_red_envelope));
      } else {
        mTvPacketType.setVisibility(View.GONE);
      }
    }
  }

  /**
   * Method name:openRedPacket
   * Describe: 打开红包
   * Create person：侯洪旭
   * Create time：16/7/29 下午3:27
   * Remarks：
   */
  private void openRedPacket(final Context context, final LCIMRedPacketMessage message) {
    final ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setCanceledOnTouchOutside(false);

    int chatType;
    if (!TextUtils.isEmpty(message.getRedPacketType())) {
      chatType = RPConstant.CHATTYPE_GROUP;
    } else {
      chatType = RPConstant.CHATTYPE_SINGLE;
    }

    RPRedPacketUtil.getInstance().openRedPacket(wrapperRedPacketInfo(chatType, message),
            (FragmentActivity) context,
            new RPRedPacketUtil.RPOpenPacketCallback() {
              @Override
              public void onSuccess(String senderId, String senderNickname, String myAmount) {
              }

              @Override
              public void showLoading() {
                progressDialog.show();
              }

              @Override
              public void hideLoading() {
                progressDialog.dismiss();
              }

              @Override
              public void onError(String code, String message) { /*错误处理*/
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
              }
            });
  }

  /**
   * 封装拆红包所需参数
   *
   * @param chatType 聊天类型
   * @param message  EMMessage
   * @return RedPacketInfo
   */
  private RedPacketInfo wrapperRedPacketInfo(int chatType, LCIMRedPacketMessage message) {
    String redPacketId = message.getRedPacketId();
    RedPacketInfo redPacketInfo = new RedPacketInfo();
    redPacketInfo.redPacketId = redPacketId;
    redPacketInfo.messageDirect = getMessageDirect(message);
    redPacketInfo.chatType = chatType;
    return redPacketInfo;
  }

  private String getMessageDirect(LCIMRedPacketMessage message) {
    String selfId = LeanchatUser.getCurrentUserId();
    String messageDirect; /*判断发送还是接收*/
    if (message.getFrom() != null && message.getFrom().equals(selfId)) {
      messageDirect = RPConstant.MESSAGE_DIRECT_SEND;
    } else {
      messageDirect = RPConstant.MESSAGE_DIRECT_RECEIVE;
    }
    return messageDirect;
  }
}
