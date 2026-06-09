
import CyberpunkCard from "./isCyberpunkCard";
import Button from './Button/Button';
import Mane from './assets/Screenshot (296).png'
import David from './assets/Screenshot (818).png'
import Lucy from './assets/Screenshot (810).png'


function App() {
  

  return (
    <>
      <div className="header">
        <Button />
      </div>
      
      <div className="card-container">
        <CyberpunkCard img={Mane} name="Mane" age={35} isCyberpunk={true} />
        <CyberpunkCard img={David} name="David Martinez" age={17} isCyberpunk={false} />
        <CyberpunkCard img={Lucy} name="Lucyna Kushinada" age={19} isCyberpunk={true}/>
        <CyberpunkCard name="Gloria Martinez" age={19} isCyberpunk={false}/>
        <CyberpunkCard name="Rebecca" age={19} isCyberpunk={true}/>
        <CyberpunkCard name="Pillar" age={30} isCyberpunk={true}/>
        <CyberpunkCard/>
      </div>
      
    </>
  )
}

export default App
