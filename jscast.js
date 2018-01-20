//ES6 arrow function
//dependencies
const express = require('express');
const app = express();
const bodyParser = require('body-parser');

//set port to listen
app.listen(3001, () => {
    console.log('listening on 3001');
});

app.set('view engine', 'ejs');
app.use(bodyParser.urlencoded({extended: true}));
app.use(express.static('public'));
app.use(bodyParser.json());

app.get('/', (req, res) => {
    console.log("Hello from hell");
    res.render('index.ejs');
});
