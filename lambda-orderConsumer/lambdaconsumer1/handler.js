'use strict';

module.exports.orderCreatedConsumer1 = async event => {
  console.log('--- Order created in lambda consumer 1! ---')
  console.log(JSON.stringify(event, null, 2))
};

