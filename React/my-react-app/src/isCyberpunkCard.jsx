
import ButtonCard from './Button/ButtonCard';
import PropTypes from 'prop-types'
import CyberpunkWP from './assets/Screenshot (700).png'


function CyberpunkCard({name, age, isCyberpunk, img, code}){

    const imgA = `Enter an Image for ${name}`; // This is just a placeholder for the alt text of the image. You can replace it with any descriptive text you want.
    
    if (typeof name !== 'string' 
        || typeof age !== 'number' 
        || typeof isCyberpunk !== 'boolean' 
        || typeof img !== 'string' 
        ) {//|| typeof code !== 'string'
        return <p>Error: Invalid prop types. Please check the console for more details. <br />
        </p>;
    } 
    else {
        return(//What you want to return will go here if the conditions are met
            <div className="card">
                
                <img src = {img} alt = {imgA} className='mane'></img>

                <h2 className='title'>CyberPunk: {code}</h2>
                <ButtonCard />
                <div className='description'>
                    <p className='Name'> Name: {name}</p>
                    <p className='Age'> Age: {age}</p>
                    <p className='isCyberpunk'> Affiliated: {isCyberpunk ? "Yes" : "No"}</p>
                </div>
            </div>
        );
    }
}

CyberpunkCard.propTypes = {
    name: PropTypes.string.isRequired,
    age: PropTypes.number.isRequired,
    isCyberpunk: PropTypes.bool.isRequired,
    img: PropTypes.string.isRequired,
    code: PropTypes.string.isRequired
}

CyberpunkCard.defaultProps = {
    name: "Unknown",
    age: 0,
    isCyberpunk: false,
    img: CyberpunkWP,
    code: "N/A"
}


export default CyberpunkCard;