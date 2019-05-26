#include <ros/ros.h>
#include <std_msgs/String.h>
#include <sensor_msgs/LaserScan.h>
#include <geometry_msgs/Twist.h>

static std::string pub_topic;
static ros::Publisher speed_pub;
static ros::Publisher message_pub;

void ScanCB(const std_msgs::LaserScan::ConstPtr &msg){
    if (msg.range_min <= 0.2) {
        geometry_msgs::Twist vel_cmd;
        vel_cmd.linear.x = 0;
        vel_cmd.linear.y = 0;
        vel_cmd.linear.z = 0;
        vel_cmd.angular.x = 0;
        vel_cmd.angular.y = 0;
        vel_cmd.angular.z = 0;
        speed_pub.publish(vel_cmd);
        std_msgs::String message;
        message.data = "stop";
        message_pub.publish(message);
    }
}

int main(int argc, char** argv)
{
    ros::init(argc,argv,"emergency_stop");

    ros::NodeHandle n;
    speed_pub = n.advertise<std_msgs::String>("/cmd_vel", 30);
    message_pub = n.advertise<std_msgs::String>("/team_203/emergency_stop"， 1);
    //ros::Subscriber speed_sub = n.subscribe("/cmd_vel", 30, speedCB);
    ros::Subscriber res_sub = n.subscribe("/scan", 30, ScanCB);
    
    ros::Rate r(30);
    while(ros::ok())
    {
        ros::spinOnce();
        r.sleep();
    }
    
    return 0;
}
