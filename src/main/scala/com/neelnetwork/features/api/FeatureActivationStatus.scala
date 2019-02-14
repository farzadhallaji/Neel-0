package com.neelnetwork.features.api

import com.neelnetwork.features.BlockchainFeatureStatus

case class FeatureActivationStatus(id: Short,
                                   description: String,
                                   blockchainStatus: BlockchainFeatureStatus,
                                   nodeStatus: NodeFeatureStatus,
                                   activationHeight: Option[Int],
                                   supportingBlocks: Option[Int])
