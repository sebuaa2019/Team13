#include "ros/ros.h"
#include "std_msgs/String.h"
//#include "/home/robot/catkin_ws/src/team_203/include/yamlsave.h"
#include <stdlib.h>
#include <string.h>
#include <iostream>
#include <fstream>
#include <tf/transform_listener.h>
#include <geometry_msgs/PoseStamped.h>
#include <yaml-cpp/yaml.h>
//#include "Quaternion.h"
#ifndef MAP_FILE_NAME
#define MAP_FILE_NAME "/home/robot/map.yaml"
#endif

namespace yaml_save{
    struct Vec3 { 
		double x, y, yaw; 
		Vec3(double x,double y,double yaw){
			this->x = x;
			this->y = y;
			this->yaw = yaw;
		}
	};
	int add_last_origin(char* filename, Vec3 v){
		YAML::Node mapyaml = YAML::LoadFile(filename);
		std::cout << mapyaml["image"] << mapyaml["origin"] << std::endl;

		YAML::Node origin_old = YAML::Load("[0.0, 0.0, 0.0]");
		origin_old[0] = mapyaml["origin"][0].as<double>();
		origin_old[1] = mapyaml["origin"][1].as<double>();
		origin_old[2] = mapyaml["origin"][2].as<double>();
		mapyaml["origin_origin"] = origin_old;
		YAML::Node origin_new = YAML::Load("[0.0, 0.0, 0.0]");
		origin_new[0] = v.x;
		origin_new[1] = v.y;
		origin_new[2] = v.yaw;
		mapyaml["last_origin"] = origin_new;
		std::ofstream fout(filename);
		fout << mapyaml;
		fout.close();
		return 0;
	}
	int use_origin(char* filename,char* origin_name){
		YAML::Node mapyaml = YAML::LoadFile(filename);
		if(mapyaml[origin_name].size() >= 3){
			YAML::Node origin_old = YAML::Load("[0.0, 0.0, 0.0]");
			origin_old[0] = mapyaml[origin_name][0].as<double>();
			origin_old[1] = mapyaml[origin_name][1].as<double>();
			origin_old[2] = mapyaml[origin_name][2].as<double>();
			mapyaml["origin"] = origin_old;
			std::ofstream fout(filename);
			fout << mapyaml;
			fout.close();
			return 0;
		}
		return -1;
	}
}


enum MAINSTATE{
	MAINSTATE_IDLE,
	
	MAINSTATE_HOLD,
	MAINSTATE_STARTSLAM,
	MAINSTATE_SLAM,
	MAINSTATE_STARTNAV,
	MAINSTATE_NAV,
	MAINSTATE_STARTGRAB,
	MAINSTATE_GRAB
};
enum MAINACTION{
	ACT_NULL,
	ACT_STARTHOLD,
	ACT_STOPHOLD,
	ACT_STARTSLAM,
	ACT_STOPSLAM,
	ACT_STARTNAV,
	ACT_STOPNAV,
	ACT_STARTGRAB,
	ACT_STOPGRAB
};
MAINSTATE CURSTATE;
MAINACTION NEXTACTION;
double x_map,y_map,yaw_map;
/**
 * This tutorial demonstrates simple receipt of messages over the ROS system.
 */
void chatterCallback(const std_msgs::String::ConstPtr& msg)//是一个回调函数，当接收到 chatter 话题的时候就会被调用。
{
  const char* cstr_msg = msg->data.c_str();
  ROS_INFO("I heard: [%s]", cstr_msg);
	if(CURSTATE == MAINSTATE_IDLE){
		if(msg->data == "start slam"){
			system("nohup roslaunch team_203 gmapping.launch &");
			ROS_INFO("SWITCH TO SLAM MODE");
			CURSTATE = MAINSTATE_SLAM;
		}
	}
	else if(CURSTATE == MAINSTATE_SLAM){
		if(msg->data == "stop slam"){
			system("rosrun map_server map_saver -f map");
			system("rosnode kill slam_gmapping");
			system("rosnode kill wpb_home_joy");
			system("rosnode kill teleop");
			system("rosnode kill rviz");
			yaml_save::Vec3 v(x_map,y_map,yaw_map);
			yaml_save::add_last_origin(MAP_FILE_NAME,v);
		}else{
			ROS_INFO("invalid command in MAINSTATE_SLAM %s",cstr_msg);
		}
	}
	
}

int main(int argc, char **argv)
{
  /**
   * The ros::init() function needs to see argc and argv so that it can perform
   * any ROS arguments and name remapping that were provided at the command line. For programmatic
   * remappings you can use a different version of init() which takes remappings
   * directly, but for most command-line programs, passing argc and argv is the easiest
   * way to do it.  The third argument to init() is the name of the node.
   *
   * You must call one of the versions of ros::init() before using any other
   * part of the ROS system.
   */
  ros::init(argc, argv, "listener");
  CURSTATE=MAINSTATE_IDLE;
  NEXTACTION=ACT_NULL;
  /**
   * NodeHandle is the main access point to communications with the ROS system.
   * The first NodeHandle constructed will fully initialize this node, and the last
   * NodeHandle destructed will close down the node.
   */
  ros::NodeHandle n;
  ROS_INFO("init mainctrl");
  /**
   * The subscribe() call is how you tell ROS that you want to receive messages
   * on a given topic.  This invokes a call to the ROS
   * master node, which keeps a registry of who is publishing and who
   * is subscribing.  Messages are passed to a callback function, here
   * called chatterCallback.  subscribe() returns a Subscriber object that you
   * must hold on to until you want to unsubscribe.  When all copies of the Subscriber
   * object go out of scope, this callback will automatically be unsubscribed from
   * this topic.
   *
   * The second parameter to the subscribe() function is the size of the message
   * queue.  If messages are arriving faster than they are being processed, this
   * is the number of messages that will be buffered up before beginning to throw
   * away the oldest ones.
   */
  ros::Subscriber sub = n.subscribe("/ctrlmsg", 1000, chatterCallback);
  ros::spin();
  ros::Rate loop_rate(40);  
  tf::StampedTransform stamped_transform;
  tf::TransformListener listener;
  while(ros::ok()){
	if(CURSTATE == MAINSTATE_SLAM){
		try
		{
			listener.lookupTransform("/map", "/base_link",ros::Time(0),stamped_transform);
			x_map =  stamped_transform.getOrigin().x();
			y_map =  stamped_transform.getOrigin().y();
			tf::Quaternion q = stamped_transform.getRotation();
			double roll,pitch,yaw;
			tf::Matrix3x3(q).getRPY(roll,pitch,yaw);
			yaw_map =  yaw;     
		}
		catch(tf::TransformException &ex){
			ROS_ERROR("%s",ex.what());
			ros::Duration(0.5).sleep();
			continue;
		}
	}
	loop_rate.sleep();


  }
	

return 0; 
}
