package com.timetracker.activities

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.LocalBroadcastManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.NotificationCompat
import android.view.*
import android.widget.*

import com.timetracker.dao.ActionDao
import com.timetracker.dao.CategoryDao
import com.timetracker.db.DbHelper
import com.timetracker.entities.Action
import com.timetracker.entities.Category
import com.timetracker.fragments.CreateCategoryDialog
import com.timetracker.services.NotificationActionService

import com.timetracker.utils.Constants.*
import com.timetracker.R
import com.timetracker.fragments.DeleteCategoryDialog
import com.timetracker.services.NotificationActionService.ACTION_CLOSE
import com.timetracker.services.NotificationActionService.ACTION_PAUSE
import com.timetracker.services.NotificationActionService.ACTION_PLAY
import com.timetracker.services.NotificationActionService.ACTION_STOP

import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

class TrackersFragment : Fragment() {

    private var dbHelper: DbHelper? = null
    private var categoryDao: CategoryDao? = null
    private var actionDao: ActionDao? = null

    private var recordsList: ListView? = null
    private var notificationBroadcastReceiver: BroadcastReceiver? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater!!.inflate(R.layout.fragment_trackers, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        (view!!.findViewById(R.id.add_category) as FloatingActionButton).setOnClickListener { _ -> createCategoryDialog() }
    }

    override fun onResume() {
        super.onResume()

        refresh()

        notificationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                refresh()
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(UPDATE_ACTION_BROADCAST)
        LocalBroadcastManager.getInstance(activity).registerReceiver(notificationBroadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(notificationBroadcastReceiver)
    }

    private fun refresh() {
        recordsList = view!!.findViewById(R.id.records_list) as ListView

        val adapter = CategoryArrayAdapter(
                activity,
                android.R.layout.simple_list_item_1,
                categoryDao!!.list())

        recordsList!!.adapter = adapter
        recordsList!!.setOnItemClickListener { parent, view, position, id ->
            val item = adapter.getItem(position)
            val chronometer = view.findViewById(R.id.category_chronometer) as Chronometer
            if (actionDao!!.switchAction(item.id) == Action.ActionType.PAUSE) {

                chronometer.stop()

                (activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
            } else {
                val base = SystemClock.elapsedRealtime() - actionDao!!.calcTodayLogged(LocalDateTime(), BEGIN_OF_DAY, item.id)!!
                chronometer.base = base
                chronometer.start()

                sendNotification(activity.applicationContext,
                        activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager,
                        true, item.id, actionDao!!, categoryDao!!)
            }
        }
    }

    private fun showStats(category: Category) {
        val intent = Intent(activity, WeeklyStats::class.java)
        intent.putExtra(WeeklyStats.CATEGORY_EXTRA_KEY, category)
        startActivity(intent)
    }

    private fun init() {
        dbHelper = DbHelper(activity.baseContext)
        categoryDao = CategoryDao(dbHelper!!)
        actionDao = ActionDao(dbHelper!!)
    }

    private fun showPopupMenu(category: Category, view: View) {
        val popup = PopupMenu(activity, view)

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener { item ->
            itemMenuClickListener(item, category)
        }
        popup.inflate(R.menu.item_menu)
        popup.show()
    }

    private fun createCategoryDialog() {
        CreateCategoryDialog { categoryName ->
            categoryDao!!.save(Category.CreateCategory(categoryName))
            refresh()
        }.show(fragmentManager, CreateCategoryDialog::javaClass.name)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = activity.menuInflater!!
        inflater.inflate(R.menu.item_menu, menu)
    }

    fun itemMenuClickListener(item: MenuItem, category: Category): Boolean {
        return when (item.itemId) {
            R.id.stats_category -> {
                showStats(category)
                true
            }
            R.id.edit_category -> {
                Toast.makeText(activity.applicationContext, "Edit clicked on " + category.name, Toast.LENGTH_SHORT).show()
                true
            }
            R.id.delete_category -> {
                deleteCategoryDialog(category.name, category.id)
                true
            }
            else -> {
                super.onContextItemSelected(item)
            }
        }
    }

    private fun deleteCategoryDialog(categoryName: String, categoryId: Int) {
        DeleteCategoryDialog(categoryName, categoryId) { cId ->
            categoryDao!!.delete(cId)
            refresh()
            Toast.makeText(activity.applicationContext, "Category '$categoryName' was deleted" , Toast.LENGTH_SHORT).show()
        }.show(fragmentManager, DeleteCategoryDialog::javaClass.name)
    }

    private inner class CategoryArrayAdapter(context: Context, resource: Int, objects: List<Category>) : ArrayAdapter<Category>(context, resource, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
            }

            val textView = convertView!!.findViewById(R.id.category_text_view) as TextView

            val chronometer = convertView.findViewById(R.id.category_chronometer) as Chronometer
            val beginOfDay = LocalTime(6, 0)
            chronometer.base = SystemClock.elapsedRealtime() - actionDao!!.calcTodayLogged(LocalDateTime(), beginOfDay, getItem(position)!!.id)!!
            if ((actionDao!!.lastCategoryAction(getItem(position)!!.id)?.type ?: (Action.ActionType.PAUSE)).equals(Action.ActionType.PLAY))
                chronometer.start()
            textView.text = getItem(position)!!.name

            val itemMenuButton = convertView.findViewById(R.id.item_menu_button) as ImageButton
            itemMenuButton.setOnClickListener { v ->
                showPopupMenu(getItem(position)!!, v)
            }

            return convertView
        }
    }

    companion object {

        var NOTIFICATION_ID = 1

        var UPDATE_ACTION_BROADCAST = "com.timetracker.UPDATE"

        fun sendNotification(context: Context,
                             notificationManager: NotificationManager,
                             chronometerStarted: Boolean,
                             categoryId: Int?, actionDao: ActionDao, categoryDao: CategoryDao) {
            val base = System.currentTimeMillis() - actionDao.calcTodayLogged(LocalDateTime(), BEGIN_OF_DAY, categoryId)!!
            val mBuilder = NotificationCompat.Builder(context)
                    .setSmallIcon(android.R.drawable.stat_notify_voicemail)
                    .setVisibility(Notification.VISIBILITY_PUBLIC) as NotificationCompat.Builder

            mBuilder.setWhen(base)

            categoryDao.findById(categoryId)?.name.let{ it -> mBuilder.setContentText(it) }

            if (chronometerStarted) {
                mBuilder.addAction(android.support.v4.app.NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pendingIntent(ACTION_PAUSE, 0, categoryId, context)))
                mBuilder.addAction(android.support.v4.app.NotificationCompat.Action(android.R.drawable.ic_delete, "Finish", pendingIntent(ACTION_STOP, 1, categoryId, context)))
                mBuilder.setOngoing(true)
                mBuilder.setUsesChronometer(true)
            } else {
                mBuilder.addAction(android.support.v4.app.NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", pendingIntent(ACTION_PLAY, 2, categoryId, context)))
                mBuilder.addAction(android.support.v4.app.NotificationCompat.Action(android.R.drawable.ic_delete, "Finish", pendingIntent(ACTION_CLOSE, 3, categoryId, context)))
                mBuilder.setOngoing(false)
                mBuilder.setShowWhen(false)
            }

            mBuilder.setStyle(NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1))

            notificationManager.notify(NOTIFICATION_ID, mBuilder.build())
        }

        private fun pendingIntent(actionName: String, requestCode: Int?, categoryId: Int?, context: Context): PendingIntent {
            val intent = Intent(context, NotificationActionService::class.java)
            intent.putExtra(NotificationActionService.CATEGORY_FIELD, categoryId)
            intent.putExtra(NotificationActionService.ACTION_NAME, actionName)
            return PendingIntent.getService(context, requestCode!! + categoryId!!, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
    }

}
