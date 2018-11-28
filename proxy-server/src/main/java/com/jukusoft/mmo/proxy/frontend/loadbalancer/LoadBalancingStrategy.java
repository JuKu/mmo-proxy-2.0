package com.jukusoft.mmo.proxy.frontend.loadbalancer;

/**
* load balancing strategies
 *
 * @link https://kemptechnologies.com/load-balancer/load-balancing-algorithms-techniques/
*/
public enum LoadBalancingStrategy {

    ROUND_ROBIN,

    RANDOM,

    LEAST_CONN,

    AGENT_BASED

}
