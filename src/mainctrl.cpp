#include "ros/ros.h"
#include "std_msgs/String.h"
//#include "/home/robot/catkin_ws/src/team_203/include/yamlsave.h"
#include <stdlib.h>
#include <string.h>
#include <iostream>
#include <fstream>
#include <semaphore.h>
#include <tf/transform_listener.h>
#include <geometry_msgs/PoseStamped.h>
#include <yaml-cpp/yaml.h>
#include <move_base_msgs/MoveBaseAction.h>
#include <actionlib/client/simple_action_client.h>
//#include "Quaternion.h"
#ifndef MAP_FILE_NAME
#define MAP_FILE_NAME "/home/robot/map.yaml"
#endif
#define USE_YAML_SAVE false
sem_t callback_lock;
namespace yaml_save
{
struct Vec3
{
    double x, y, yaw;
    Vec3(double x, double y, double yaw)
    {
	this->x = x;
	this->y = y;
	this->yaw = yaw;
    }
};
int add_last_origin(char *filename, Vec3 v)
{
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
int use_origin(char *filename, char *origin_name)
{
    YAML::Node mapyaml = YAML::LoadFile(filename);
    if (mapyaml[origin_name].size() >= 3)
    {
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

enum MAINSTATE
{
    MAINSTATE_IDLE,

    MAINSTATE_HOLD,
    MAINSTATE_STARTSLAM,
    MAINSTATE_SLAM,
    MAINSTATE_STARTNAV,
    MAINSTATE_NAV,
    MAINSTATE_STARTGRAB,
    MAINSTATE_GRAB
};
enum MAINACTION
{
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
typedef actionlib::SimpleActionClient<move_base_msgs::MoveBaseAction> MoveBaseClient;

MoveBaseClient *moveBaseClient;
double x_map, y_map, yaw_map;
/**
 * This tutorial demonstrates simple receipt of messages over the ROS system.
 */
void chatterCallback(const std_msgs::String::ConstPtr &msg) //是一个回调函数，当接收到 chatter 话题的时候就会被调用.
{
    sem_wait(&callback_lock);
    const char *cstr_msg = msg->data.c_str();
    ROS_INFO("I heard: [%s]", cstr_msg);
    if (CURSTATE == MAINSTATE_IDLE)
    {
	if (msg->data == "start slam")
	{
	    system("nohup roslaunch team_203 gmapping.launch &");
	    ROS_INFO("SWITCH TO SLAM MODE");
	    CURSTATE = MAINSTATE_SLAM;
	    ros::Duration(1.0).sleep();
	}
	if (msg->data == "start nav origin")
	{
		if(USE_YAML_SAVE)
	    yaml_save::use_origin(MAP_FILE_NAME, "origin_origin");
	    system("nohup roslaunch team_203 nav.launch &");
	    //moveBaseClient = MoveBaseClient("move_base", true);
	    while (!moveBaseClient->waitForServer(ros::Duration(5.0)))
	    {
		ROS_INFO("Waiting for the move_base action server to come up");
	    }
		CURSTATE = MAINSTATE_NAV;
	}
	if (msg->data == "start nav last")
	{
		if(USE_YAML_SAVE)
	    yaml_save::use_origin(MAP_FILE_NAME, "last_origin");
	    system("nohup roslaunch team_203 nav.launch &");
	    //moveBaseClient = MoveBaseClient("move_base", true);
	    while (!moveBaseClient->waitForServer(ros::Duration(5.0)))
	    {
		ROS_INFO("Waiting for the move_base action server to come up");
	    }
		CURSTATE = MAINSTATE_NAV;
	}
	if (msg->data == "start grab")
	{
	}
    }
    else if (CURSTATE == MAINSTATE_SLAM)
    {
	if (msg->data == "stop slam")
	{
	    tf::StampedTransform stamped_transform;
	    tf::TransformListener listener;
	    try
	    {
		listener.waitForTransform("/map", "/base_footprint", ros::Time(0), ros::Duration(2.0));
		listener.lookupTransform("/base_footprint", "/odom", ros::Time(0), stamped_transform);
		x_map = stamped_transform.getOrigin().x();
		y_map = stamped_transform.getOrigin().y();
		tf::Quaternion q = stamped_transform.getRotation();
		double roll, pitch, yaw;
		tf::Matrix3x3(q).getRPY(roll, pitch, yaw);
		yaw_map = yaw;
	    }
	    catch (tf::TransformException &ex)
	    {
		ROS_ERROR("%s", ex.what());
	    }
	    if (USE_YAML_SAVE)
	    {
		yaml_save::Vec3 v(x_map, y_map, yaw_map);
		yaml_save::add_last_origin(MAP_FILE_NAME, v);
	    }
	    ROS_INFO("curent x,y: %f %f", (float)x_map, (float)y_map);
	    system("rosrun map_server map_saver -f map");
	    system("rosnode kill slam_gmapping");
	    system("rosnode kill wpb_home_joy");
	    system("rosnode kill teleop");
	    system("rosnode kill rviz");
	    system("rosnode kill rplidarNode");
	    CURSTATE = MAINSTATE_IDLE;
	    ros::Duration(1.0).sleep();
	    ROS_INFO("stop slam");
	}
	else
	{
	    ROS_INFO("invalid command in MAINSTATE_SLAM %s", cstr_msg);
	}
    }
    else if (CURSTATE == MAINSTATE_NAV)
    {
	if (msg->data == "stop nav")
	{
		if(USE_YAML_SAVE){
	    yaml_save::Vec3 v(x_map, y_map, yaw_map);
	    yaml_save::add_last_origin(MAP_FILE_NAME, v);
		}
	    system("rosnode kill rviz");
	    system("rosnode kill map_server");
	    system("rosnode kill rplidarNode");
	    system("rosnode kill move_base");
	    system("rosnode kill amcl");
	    CURSTATE = MAINSTATE_IDLE;
	    ROS_INFO("stop nav");
	}
	else
	{
	    float xmove, ymove;
	    int ret = sscanf(msg->data.c_str(), "movebase %f %f", &xmove, &ymove);
	    if (ret == 2)
	    {
		move_base_msgs::MoveBaseGoal goal;
		//we'll send a goal to the robot to move 1 meter forward
		goal.target_pose.header.frame_id = "map";
		goal.target_pose.header.stamp = ros::Time::now();
		goal.target_pose.pose.position.x = xmove;
		goal.target_pose.pose.position.y = ymove;
		goal.target_pose.pose.orientation.w = 1.0;
		ROS_INFO("Sending goal");
		moveBaseClient->sendGoal(goal);
		moveBaseClient->waitForResult();

		if (moveBaseClient->getState() == actionlib::SimpleClientGoalState::SUCCEEDED)
		    ROS_INFO("Hooray, the base moved 1 meter forward");
		else
		    ROS_INFO("The base failed to move forward 1 meter for some reason");
	    }
	    else
	    {
		ROS_INFO("invalid command in MAINSTATE_NAV %s", cstr_msg);
	    }
	}
    }
    sem_post(&callback_lock);
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
    sem_init(&callback_lock, 0, 1);
    ros::init(argc, argv, "listener");
    CURSTATE = MAINSTATE_IDLE;
    NEXTACTION = ACT_NULL;
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
    moveBaseClient = new MoveBaseClient("move_base", true);
    ros::Subscriber sub = n.subscribe("/ctrlmsg", 1000, chatterCallback);
    ros::spin();
    /*
    ROS_INFO("start receiving tf");
    ros::Rate loop_rate(40);
    tf::StampedTransform stamped_transform;
    tf::TransformListener listener;
    while (ros::ok())
    {
	if (CURSTATE == MAINSTATE_SLAM)
	{
	    try
	    {
		listener.lookupTransform("/map", "/base_link", ros::Time(0), stamped_transform);
		x_map = stamped_transform.getOrigin().x();
		y_map = stamped_transform.getOrigin().y();
		tf::Quaternion q = stamped_transform.getRotation();
		double roll, pitch, yaw;
		tf::Matrix3x3(q).getRPY(roll, pitch, yaw);
		yaw_map = yaw;
	    }
	    catch (tf::TransformException &ex)
	    {
		ROS_ERROR("%s", ex.what());
		ros::Duration(0.5).sleep();
		continue;
	    }
	}
	loop_rate.sleep();
    }
	*/

    return 0;
}
