#include<ros/ros.h>
#include<move_base_msgs/MoveBaseAction.h>
#include<actionlib/client/simple_action_client.h>

typedef actionlib::SimpleActionClient<move_base_msgs::MoveBaseAction>MoveBaseClient;

int main(int argc,char **argv){
    ros::init(argc,argv,"send_goal_node");

    MoveBaseClient ac("move_base",true);

    ROS_INFO("Waiting for the move_base action server");
    ac.waitForServer(ros::Duration(60));
    ROS_INFO("Connected to move base server");

    move_base_msgs::MoveBaseGoal goal;

    goal.target_pose.header.frame_id ="map";
    goal.target_pose.header.stamp =ros::Time::now();
    goal.target_pose.pose.position.x = 21.174;
    goal.target_pose.pose.position.y = 10.876;
    goal.target_pose.pose.orientation.w = 1;
    
    ROS_INFO("");
    ROS_INFO("Sending goal");
    ac.sendGoal(goal);

    ac.waitForResult();
    if(ac.getState() == actionlib::SimpleClientGoalState::SUCCEEDED)
        ROS_INFO("You have reached the goal!");
    else
        ROS_INFO("the base failed for some reason");

    return 0;
}