import amqp from "amqplib";
import { MQConfig } from "./MQConfig.js";

class MessagingQueue {
    connection = null;

    create = async () => {
        let retries = 5;
        while (retries) {
            try {
                this.connection = await amqp.connect(MQConfig.URL);
                this.connectionEvent();
                console.log("RabbitMQ Connected!");
                return this.connection;
            } catch (err) {
                console.error(`RabbitMQ connection failed. Retrying... (${5 - retries + 1}/5)`, err);
                retries -= 1;
                if (!retries) throw err;
                await new Promise(res => setTimeout(res, 2000));
            }
        }
    }

    createChannel = async (key) => {
        return await this.connection.createChannel(key);
    }

    connectionEvent = () => {
        this.connection.on("close", () => {
            console.log("Connection closed");
        });

        this.connection.on("error", (error) => {
            console.log("Connection error", error);
        });
    }
}

const mq = new MessagingQueue();

export default mq;