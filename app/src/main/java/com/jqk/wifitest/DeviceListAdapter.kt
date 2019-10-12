package com.jqk.wifitest

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jqk.wifitest.databinding.ItemDeviceBinding

class DeviceListAdapter : RecyclerView.Adapter<DeviceListAdapter.MViewHolder> {
    val content: Context
    val datas: List<WifiP2pDevice>

    constructor(content: Context, datas: List<WifiP2pDevice>) : super() {
        this.content = content
        this.datas = datas
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        val binding = DataBindingUtil.inflate<ItemDeviceBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_device,
            parent,
            false
        )
        return MViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        DataBindingUtil.getBinding<ItemDeviceBinding>(holder.itemView)?.run {
            name.text = datas[position].deviceName
            status.text = Util.getDeviceStatus(datas[position].status)

            itemView.setOnClickListener{
                onClickListener?.onClick(datas[position])
            }
        }
    }

    inner class MViewHolder : RecyclerView.ViewHolder {
        constructor(itemView: View) : super(itemView)
    }

    interface OnClickListener {
        fun onClick(wifiP2pDevice: WifiP2pDevice)
    }

    var onClickListener: OnClickListener? = null
}