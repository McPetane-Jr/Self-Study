import styles from './Button.module.css';

function Button(){
    return(
        <button className={styles.btn}>
            {"\u{1F50D}"} {/*Here is how to comment in jsx:*/}
            </button>
    );
}

export default Button;