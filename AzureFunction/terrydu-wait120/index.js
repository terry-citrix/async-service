module.exports = async function (context, req) {
    context.log('JavaScript HTTP trigger function processed a request.');

    await new Promise(resolve => setTimeout(resolve, 2 * 60 * 1000));

    const name = (req.query.name || (req.body && req.body.name));
    const responseMessage = name
        ? "Hello, " + name
        : "Hello, person " + Math.floor(Math.random() * 1000);

    context.res = {
        // status: 200, /* Defaults to 200 */
        body: responseMessage
    };
}
